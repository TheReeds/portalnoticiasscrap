import './Pricing.css'

function Pricing() {
    return (
        <div className="pricing-container">
            <h1>Planes de Suscripción</h1>
            <div className="pricing-grid">
                <div className="pricing-card free">
                    <h2>Gratis</h2>
                    <div className="price">$0<span>/mes</span></div>
                    <ul>
                        <li>Acceso a noticias recientes</li>
                        <li>Búsqueda básica</li>
                        <li>Publicidad incluida</li>
                    </ul>
                    <button className="plan-btn">Actual</button>
                </div>
                <div className="pricing-card premium">
                    <h2>Premium</h2>
                    <div className="price">$9.99<span>/mes</span></div>
                    <ul>
                        <li>Todo lo de Gratis</li>
                        <li>Analítica Avanzada</li>
                        <li>Predicción con IA</li>
                        <li>Sin publicidad</li>
                        <li>Soporte prioritario</li>
                    </ul>
                    <button className="plan-btn primary">Suscribirse</button>
                </div>
            </div>
        </div>
    )
}

export default Pricing
