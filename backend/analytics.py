import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import KMeans
from typing import List, Dict, Any
from datetime import datetime

# Spanish stopwords (same as in ml_model)
SPANISH_STOPWORDS = [
    'de', 'la', 'que', 'el', 'en', 'y', 'a', 'los', 'del', 'se', 'las', 'por', 'un', 'para', 'con', 'no', 'una', 'su', 'al', 'lo', 'como',
    'más', 'pero', 'sus', 'le', 'ya', 'o', 'este', 'sí', 'porque', 'esta', 'entre', 'cuando', 'muy', 'sin', 'sobre', 'también', 'me', 'hasta',
    'hay', 'donde', 'quien', 'desde', 'todo', 'nos', 'durante', 'todos', 'uno', 'les', 'ni', 'contra', 'otros', 'ese', 'eso', 'ante', 'ellos',
    'e', 'esto', 'mí', 'antes', 'algunos', 'qué', 'unos', 'yo', 'otro', 'otras', 'otra', 'él', 'tanto', 'esa', 'estos', 'mucho', 'quienes',
    'nada', 'muchos', 'cual', 'poco', 'ella', 'estar', 'estas', 'algunas', 'algo', 'nosotros', 'mi', 'mis', 'tú', 'te', 'ti', 'tu', 'tus',
    'ellas', 'nosotras', 'vosotros', 'vosotras', 'os', 'mío', 'mía', 'míos', 'mías', 'tuyo', 'tuya', 'tuyos', 'tuyas', 'suyo', 'suya',
    'suyos', 'suyas', 'nuestro', 'nuestra', 'nuestros', 'nuestras', 'vuestro', 'vuestra', 'vuestros', 'vuestras', 'es', 'son', 'fue', 'era'
]

class ArticleAnalytics:
    def __init__(self, articles_data: List[Dict]):
        self.df = pd.DataFrame(articles_data)
        if not self.df.empty:
            self.df['published_date'] = pd.to_datetime(self.df['published_date'])
            self.df['date'] = self.df['published_date'].dt.date

    def perform_clustering(self, n_clusters: int = 5) -> Dict[str, Any]:
        if self.df.empty or len(self.df) < n_clusters:
            return {"total_articles": len(self.df), "clusters": []}

        # Prepare text
        self.df['text'] = self.df['title'].fillna('') + " " + self.df['summary'].fillna('')
        
        # Vectorize
        vectorizer = TfidfVectorizer(
            max_features=1000,
            stop_words=SPANISH_STOPWORDS,
            min_df=2
        )
        
        try:
            tfidf_matrix = vectorizer.fit_transform(self.df['text'])
        except ValueError:
            # Not enough data for vectorization
            return {"total_articles": len(self.df), "clusters": []}

        # Clustering
        actual_n_clusters = min(n_clusters, len(self.df))
        kmeans = KMeans(n_clusters=actual_n_clusters, random_state=42, n_init=10)
        kmeans.fit(tfidf_matrix)
        
        self.df['cluster'] = kmeans.labels_
        
        # Extract keywords for each cluster
        feature_names = vectorizer.get_feature_names_out()
        clusters_data = []
        
        for i in range(actual_n_clusters):
            cluster_center = kmeans.cluster_centers_[i]
            # Get top 5 keywords
            top_indices = cluster_center.argsort()[-5:][::-1]
            keywords = [feature_names[ind] for ind in top_indices]
            
            cluster_articles = self.df[self.df['cluster'] == i]
            
            clusters_data.append({
                "id": int(i),
                "size": int(len(cluster_articles)),
                "keywords": keywords,
                "percentage": float(len(cluster_articles) / len(self.df) * 100),
                "sample_titles": cluster_articles['title'].head(3).tolist()
            })
            
        return {
            "total_articles": len(self.df),
            "clusters": clusters_data
        }

    def get_cluster_trends(self) -> List[Dict]:
        if self.df.empty or 'cluster' not in self.df.columns:
            return []
            
        cluster_trends = self.df.groupby(['date', 'cluster']).size().unstack(fill_value=0)
        
        trends_data = []
        for date, row in cluster_trends.iterrows():
            data_point = {"date": date.strftime("%Y-%m-%d")}
            for cluster_id in row.index:
                data_point[f"cluster_{cluster_id}"] = int(row[cluster_id])
            trends_data.append(data_point)
            
        return trends_data
