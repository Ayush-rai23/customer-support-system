import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { login as apiLogin, logout as apiLogout, fetchCurrentAdmin } from '../api/auth'
import { setUnauthorizedHandler } from '../api/axios'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [admin, setAdmin] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setUnauthorizedHandler(() => setAdmin(null))
  }, [])

  useEffect(() => {
    fetchCurrentAdmin()
      .then(setAdmin)
      .catch(() => setAdmin(null))
      .finally(() => setLoading(false))
  }, [])

  const login = useCallback(async (email, password) => {
    const data = await apiLogin(email, password)
    setAdmin(data)
    return data
  }, [])

  const logout = useCallback(async () => {
    await apiLogout()
    setAdmin(null)
  }, [])

  const value = { admin, isAuthenticated: !!admin, loading, login, logout }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
