import { useState } from 'react';
import { useAuth } from './AuthProvider';
import { useNavigate, useSearchParams } from 'react-router-dom';

export default function VerifyPage() {
    const { verify } = useAuth();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const email = searchParams.get('email') || '';
    const [otp, setOtp] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await verify(email, otp);
            navigate('/login');
        } catch (err) {
            setError('Verification failed. Invalid OTP?');
        }
    };

    return (
        <div className="auth-container">
            <h2>Verify OTP</h2>
            <p>Enter the OTP sent to {email}</p>
            {error && <p className="error">{error}</p>}
            <form onSubmit={handleSubmit}>
                <input type="text" placeholder="OTP" value={otp} onChange={e => setOtp(e.target.value)} required />
                <button type="submit">Verify</button>
            </form>
        </div>
    );
}
