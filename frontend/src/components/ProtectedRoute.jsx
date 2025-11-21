import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

function ProtectedRoute({ children, adminOnly = false, allowPremium = false }) {
    const { user, loading } = useAuth()
    const location = useLocation()

    if (loading) {
        return <div>Cargando...</div>
    }

    if (!user) {
        return <Navigate to="/login" state={{ from: location }} replace />
    }

    if (adminOnly && !user.is_admin) {
        return <Navigate to="/" replace />
    }

    if (allowPremium && !user.is_admin && user.subscription_plan !== 'premium') {
        return <Navigate to="/" replace />
    }

    return children
}

export default ProtectedRoute
