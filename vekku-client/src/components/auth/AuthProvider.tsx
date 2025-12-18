import React, { createContext, useContext, useState, useEffect } from 'react';

interface User {
    email: string;
    accessToken: string;
}

interface AuthContextType {
    user: User | null;
    signup: (data: any) => Promise<void>;
    verify: (email: string, otp: string) => Promise<void>;
    login: (data: any) => Promise<void>;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        const email = localStorage.getItem('userEmail');
        if (token && email) {
            setUser({ email, accessToken: token });
        }
    }, []);

    const signup = async (data: any) => {
        const response = await fetch('http://localhost:8080/api/auth/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!response.ok) throw new Error('Signup failed');
    };

    const verify = async (email: string, otp: string) => {
        const response = await fetch('http://localhost:8080/api/auth/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, otp }),
        });
        if (!response.ok) throw new Error('Verification failed');
    };

    const login = async (data: any) => {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!response.ok) throw new Error('Login failed');

        const result = await response.json();
        localStorage.setItem('accessToken', result.accessToken);
        localStorage.setItem('refreshToken', result.refreshToken);
        localStorage.setItem('userEmail', data.email);
        setUser({ email: data.email, accessToken: result.accessToken });
    };

    const logout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userEmail');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, signup, verify, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within an AuthProvider');
    return context;
};
