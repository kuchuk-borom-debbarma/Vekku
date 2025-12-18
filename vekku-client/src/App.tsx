import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Home from './Home';
import { AuthProvider } from './components/auth/AuthProvider';
import SignupPage from './components/auth/SignupPage';
import VerifyPage from './components/auth/VerifyPage';
import LoginPage from './components/auth/LoginPage';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import CreateContent from './pages/CreateContent';
import ViewDoc from './pages/ViewDoc';
import DocsList from './pages/DocsList';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={
            <ProtectedRoute>
              <Home />
            </ProtectedRoute>
          } />
          <Route path="/create" element={
            <ProtectedRoute>
              <CreateContent />
            </ProtectedRoute>
          } />
          <Route path="/docs" element={
            <ProtectedRoute>
              <DocsList />
            </ProtectedRoute>
          } />
          <Route path="/docs/:id" element={
            <ProtectedRoute>
              <ViewDoc />
            </ProtectedRoute>
          } />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/verify" element={<VerifyPage />} />
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
