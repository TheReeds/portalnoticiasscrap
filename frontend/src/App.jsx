import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Navbar from './components/Navbar'
import Layout from './components/Layout'
import Home from './pages/Home'
import ArticleDetail from './pages/ArticleDetail'
import Admin from './pages/Admin'
import HistoricalNews from './pages/HistoricalNews'
import Login from './pages/Login'
import Register from './pages/Register'
import Pricing from './pages/Pricing'
import './App.css'

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="App">
          <Routes>
            {/* Rutas con Navbar */}
            <Route element={<Layout />}>
              <Route path="/" element={<Home />} />
              <Route path="/article/:id" element={<ArticleDetail />} />
              <Route
                path="/admin"
                element={
                  <ProtectedRoute adminOnly={false} allowPremium={true}>
                    <Admin />
                  </ProtectedRoute>
                }
              />
              <Route path="/historical" element={<HistoricalNews />} />
              <Route path="/pricing" element={<Pricing />} />
            </Route>

            {/* Rutas sin Navbar */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
          </Routes>
        </div>
      </AuthProvider>
    </Router>
  )
}

export default App
