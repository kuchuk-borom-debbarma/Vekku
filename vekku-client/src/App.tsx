import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './Home';
import { AuthProvider } from './components/auth/AuthProvider';
import SignupPage from './components/auth/SignupPage';
import VerifyPage from './components/auth/VerifyPage';
import LoginPage from './components/auth/LoginPage';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import ViewDoc from './pages/ViewDoc';
import DocsList from './pages/DocsList';
import ManageTags from './pages/ManageTags';
import './App.css';

import { MantineProvider, createTheme, Loader, Center } from '@mantine/core';
import { Notifications } from '@mantine/notifications';
import '@mantine/core/styles.css';
import '@mantine/tiptap/styles.css';
import '@mantine/notifications/styles.css';

const CreateContentPage = lazy(() => import('./pages/CreateContentPage'));

const theme = createTheme({
  primaryColor: 'violet',
  defaultRadius: 'md',
});

function App() {
  return (
    <MantineProvider theme={theme} defaultColorScheme="dark">
      <Notifications />
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
                <Suspense fallback={<Center h="100vh"><Loader /></Center>}>
                  <CreateContentPage />
                </Suspense>
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
            <Route path="/tags" element={
              <ProtectedRoute>
                <ManageTags />
              </ProtectedRoute>
            } />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/verify" element={<VerifyPage />} />
            <Route path="/login" element={<LoginPage />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </MantineProvider>
  );
}


export default App;
