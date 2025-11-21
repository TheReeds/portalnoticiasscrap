import { useState, useEffect } from 'react'
import axios from 'axios'
import './PredictionPanel.css'

function PredictionPanel() {
    const [text, setText] = useState('')
    const [result, ZS] = useState(null)
    const [loading, setLoading] = useState(false)
    const [modelStatus, setModelStatus] = useState(null)
    const [training, setTraining] = useState(false)

    useEffect(() => {
        fetchModelStatus()
    }, [])

    const fetchModelStatus = async () => {
        try {
            const response = await axios.get('/api/ml/status')
            setModelStatus(response.data)
        } catch (error) {
            console.error('Error fetching model status:', error)
        }
    }

    const handleTrain = async () => {
        setTraining(true)
        try {
            await axios.post('/api/ml/train')
            alert('Entrenamiento iniciado en segundo plano. Esto puede tomar unos minutos.')
            // Poll for status update after a while
            setTimeout(fetchModelStatus, 5000)
        } catch (error) {
            console.error('Error starting training:', error)
            alert('Error al iniciar el entrenamiento')
        } finally {
            setTraining(false)
        }
    }

    const handlePredict = async () => {
        if (!text.trim()) return

        setLoading(true)
        try {
            const response = await axios.post('/api/ml/predict', null, {
                params: { text }
            })
            ZS(response.data)
        } catch (error) {
            console.error('Error predicting:', error)
            alert('Error al realizar la predicción')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="prediction-panel">
            <div className="prediction-header">
                <h2>Predicción con IA</h2>
                <div className="model-status">
                    <span>Estado del Modelo: </span>
                    <span className={`status-badge ${modelStatus?.trained ? 'trained' : 'untrained'}`}>
                        {modelStatus?.trained ? 'Entrenado' : 'No Entrenado'}
                    </span>
                    {modelStatus?.last_trained && (
                        <span className="last-trained">
                            (Última vez: {new Date(modelStatus.last_trained).toLocaleDateString()})
                        </span>
                    )}
                    <button
                        className="train-btn"
                        onClick={handleTrain}
                        disabled={training}
                    >
                        {training ? 'Entrenando...' : 'Entrenar Modelo'}
                    </button>
                </div>
            </div>

            <div className="prediction-content">
                <div className="input-section">
                    <h3>Analizar Texto</h3>
                    <textarea
                        value={text}
                        onChange={(e) => setText(e.target.value)}
                        placeholder="Pega aquí el título o contenido de una noticia para analizar..."
                        rows={5}
                    />
                    <button
                        className="predict-btn"
                        onClick={handlePredict}
                        disabled={loading || !text.trim() || !modelStatus?.trained}
                    >
                        {loading ? 'Analizando...' : 'Predecir Categoría y Tendencia'}
                    </button>
                    {!modelStatus?.trained && (
                        <p className="warning-text">Debes entrenar el modelo antes de realizar predicciones.</p>
                    )}
                </div>

                {result && (
                    <div className="result-section">
                        <h3>Resultados del Análisis</h3>
                        <div className="result-cards">
                            <div className="result-card category">
                                <h4>Categoría Predicha</h4>
                                <div className="prediction-value">{result.category}</div>
                                <div className="confidence-bar">
                                    <div
                                        className="fill"
                                        style={{ width: `${result.confidence * 100}%` }}
                                    ></div>
                                </div>
                                <span className="confidence-text">
                                    Confianza: {(result.confidence * 100).toFixed(1)}%
                                </span>
                            </div>

                            <div className="result-card trend">
                                <h4>Potencial de Tendencia</h4>
                                <div className="prediction-value">
                                    {result.trend_potential > 0.7 ? '🔥 Alta' : result.trend_potential > 0.4 ? '📈 Media' : '📉 Baja'}
                                </div>
                                <div className="confidence-bar">
                                    <div
                                        className="fill trend-fill"
                                        style={{ width: `${result.trend_potential * 100}%` }}
                                    ></div>
                                </div>
                                <span className="confidence-text">
                                    Score: {(result.trend_potential * 100).toFixed(1)}%
                                </span>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    )
}

export default PredictionPanel
