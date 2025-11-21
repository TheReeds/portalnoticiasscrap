import { createContext, useState, useEffect, useContext } from 'react'
import axios from 'axios'

const AuthContext = createContext()

export function useAuth() {
    return useContext(AuthContext)
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        checkAuth()
    }, [])

    const checkAuth = async () => {
        const token = localStorage.getItem('token')
        if (!token) {
            setLoading(false)
            return
        }

        try {
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
            const response = await axios.get('/api/users/me')
            setUser(response.data)
        } catch (error) {
            localStorage.removeItem('token')
            delete axios.defaults.headers.common['Authorization']
        } finally {
            setLoading(false)
        }
    }

    const login = async (username, password) => {
        const formData = new FormData()
        formData.append('username', username)
        formData.append('password', password)

        const response = await axios.post('/api/token', formData)
        const token = response.data.access_token

        localStorage.setItem('token', token)
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
        await checkAuth()
        return true
    }

    const register = async (userData) => {
        await axios.post('/api/register', userData)
        return true
    }

    const logout = () => {
        localStorage.removeItem('token')
        delete axios.defaults.headers.common['Authorization']
        setUser(null)
    }

    const value = {
        user,
        login,
        register,
        logout,
        loading
    }

    return (
        <AuthContext.Provider value={value}>
            {!loading && children}
        </AuthContext.Provider>
    )
}
