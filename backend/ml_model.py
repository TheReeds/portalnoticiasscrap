import re
import numpy as np
import pandas as pd
import joblib
import os
from datetime import datetime
from typing import List, Dict, Any
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score

# Spanish stopwords (simplified list)
SPANISH_STOPWORDS = [
    'de', 'la', 'que', 'el', 'en', 'y', 'a', 'los', 'del', 'se', 'las', 'por', 'un', 'para', 'con', 'no', 'una', 'su', 'al', 'lo', 'como',
    'más', 'pero', 'sus', 'le', 'ya', 'o', 'este', 'sí', 'porque', 'esta', 'entre', 'cuando', 'muy', 'sin', 'sobre', 'también', 'me', 'hasta',
    'hay', 'donde', 'quien', 'desde', 'todo', 'nos', 'durante', 'todos', 'uno', 'les', 'ni', 'contra', 'otros', 'ese', 'eso', 'ante', 'ellos',
    'e', 'esto', 'mí', 'antes', 'algunos', 'qué', 'unos', 'yo', 'otro', 'otras', 'otra', 'él', 'tanto', 'esa', 'estos', 'mucho', 'quienes',
    'nada', 'muchos', 'cual', 'poco', 'ella', 'estar', 'estas', 'algunas', 'algo', 'nosotros', 'mi', 'mis', 'tú', 'te', 'ti', 'tu', 'tus',
    'ellas', 'nosotras', 'vosotros', 'vosotras', 'os', 'mío', 'mía', 'míos', 'mías', 'tuyo', 'tuya', 'tuyos', 'tuyas', 'suyo', 'suya',
    'suyos', 'suyas', 'nuestro', 'nuestra', 'nuestros', 'nuestras', 'vuestro', 'vuestra', 'vuestros', 'vuestras', 'es', 'son', 'fue', 'era'
]

class NewsPredictor:
    def __init__(self):
        self.models_dir = "models"
        if not os.path.exists(self.models_dir):
            os.makedirs(self.models_dir)
            
        self.category_model_path = os.path.join(self.models_dir, "category_model.joblib")
        self.category_vectorizer_path = os.path.join(self.models_dir, "category_vectorizer.joblib")
        self.trend_model_path = os.path.join(self.models_dir, "trend_model.joblib")
        
        self.load_models()

    def load_models(self):
        try:
            self.category_model = joblib.load(self.category_model_path)
            self.category_vectorizer = joblib.load(self.category_vectorizer_path)
            self.is_trained = True
        except:
            self.category_model = None
            self.category_vectorizer = None
            self.is_trained = False

    def preprocess_text(self, text: str) -> str:
        if not isinstance(text, str):
            return ""
        text = text.lower()
        text = re.sub(r'[^\\w\\s]', '', text)
        return text

    def extract_features(self, text: str) -> Dict[str, float]:
        features = {}
        features['length'] = len(text)
        features['word_count'] = len(text.split())
        features['has_numbers'] = 1 if re.search(r'\\d', text) else 0
        features['exclamation_count'] = text.count('!')
        features['question_count'] = text.count('?')
        features['avg_word_length'] = np.mean([len(word) for word in text.split()]) if text.split() else 0
        return features

    def train_category_model(self, articles: List[Dict]) -> Dict[str, Any]:
        df = pd.DataFrame(articles)
        
        # Filter out 'General' category if possible to make it more specific
        df_filtered = df[df['category'] != 'General']
        if len(df_filtered) < 10:
            # Fallback to using all if not enough specific data
            if len(df) < 10:
                return {"error": "Not enough training data (need at least 10 articles)"}
        else:
            df = df_filtered

        df['text'] = df['title'].fillna('') + " " + df['summary'].fillna('')
        df['clean_text'] = df['text'].apply(self.preprocess_text)
        
        self.categories = df['category'].unique().tolist()
        if len(self.categories) < 2:
            return {"error": "Need at least 2 different categories for classification"}

        self.category_vectorizer = TfidfVectorizer(
            max_features=1000,
            stop_words=SPANISH_STOPWORDS,
            min_df=2,
            max_df=0.95
        )
        
        X = self.category_vectorizer.fit_transform(df['clean_text'])
        y = df['category']
        
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
        
        self.category_model = MultinomialNB(alpha=0.1)
        self.category_model.fit(X_train, y_train)
        
        y_pred = self.category_model.predict(X_test)
        accuracy = accuracy_score(y_test, y_pred)
        
        # Save models
        joblib.dump(self.category_model, self.category_model_path)
        joblib.dump(self.category_vectorizer, self.category_vectorizer_path)
        self.is_trained = True
        
        return {
            "status": "success",
            "accuracy": float(accuracy),
            "categories": self.categories,
            "training_samples": len(df)
        }

    def predict_category(self, text: str) -> Dict[str, Any]:
        if not self.is_trained:
            return {"error": "Model not trained yet"}
            
        clean_text = self.preprocess_text(text)
        X = self.category_vectorizer.transform([clean_text])
        
        # Get probabilities
        probs = self.category_model.predict_proba(X)[0]
        best_idx = np.argmax(probs)
        predicted_category = self.category_model.classes_[best_idx]
        confidence = float(probs[best_idx])
        
        # Simple trend heuristic (placeholder for actual trend model)
        features = self.extract_features(text)
        trend_score = min(1.0, (features['views'] if 'views' in features else 0) / 1000.0) # Placeholder
        # Better trend heuristic based on text features
        trend_potential = 0.5
        if features['has_numbers']: trend_potential += 0.1
        if features['question_count'] > 0: trend_potential += 0.1
        if features['exclamation_count'] > 0: trend_potential += 0.1
        trend_potential = min(0.95, trend_potential)

        return {
            "category": predicted_category,
            "confidence": confidence,
            "trend_potential": trend_potential,
            "is_news": True  # Simplified
        }

    def get_model_status(self) -> Dict[str, Any]:
        return {
            "trained": self.is_trained,
            "last_trained": datetime.fromtimestamp(os.path.getmtime(self.category_model_path)).isoformat() if self.is_trained else None
        }
