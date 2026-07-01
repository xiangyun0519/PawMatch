import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { message } from 'antd';
import { useAuthStore } from '../stores/authStore';
import { authApi } from '../api/auth';

export default function Login() {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    rememberMe: false,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.username || !formData.password) {
      message.error('请输入用户名和密码');
      return;
    }

    setLoading(true);
    
    const response = await authApi.login({
      username: formData.username,
      password: formData.password,
    }).catch((err) => {
      message.error(err.message || '登录失败');
      return null;
    });

    setLoading(false);

    if (response?.data) {
      login(response.data, response.data.token);
      message.success('登录成功！');
      navigate('/');
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', fontFamily: '"Noto Sans SC", sans-serif' }}>
      {/* Left Panel - Branding */}
      <div
        style={{
          flex: 1,
          background: 'linear-gradient(160deg, #FF8C42 0%, #FF6B35 50%, #FFB347 100%)',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          padding: '60px',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            position: 'absolute',
            top: '-100px',
            right: '-100px',
            width: '400px',
            height: '400px',
            background: 'rgba(255,255,255,0.1)',
            borderRadius: '50%',
          }}
        />
        <div
          style={{
            position: 'absolute',
            bottom: '-80px',
            left: '-80px',
            width: '300px',
            height: '300px',
            background: 'rgba(255,255,255,0.08)',
            borderRadius: '50%',
          }}
        />
        <div style={{ textAlign: 'center', zIndex: 1, maxWidth: '440px', width: '100%' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '16px', marginBottom: '40px' }}>
            <div
              style={{
                width: '72px',
                height: '72px',
                background: 'rgba(255,255,255,0.2)',
                borderRadius: '20px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '40px',
                backdropFilter: 'blur(10px)',
              }}
            >
              🐾
            </div>
            <span style={{ fontSize: '40px', fontWeight: 700, color: '#fff' }}>PawMatch</span>
          </div>
          <h1 style={{ fontSize: '48px', color: '#fff', fontWeight: 700, marginBottom: '16px', lineHeight: 1.3 }}>
            每一次相遇
            <br />
            都是最好的安排
          </h1>
          <p style={{ fontSize: '18px', color: 'rgba(255,255,255,0.9)', lineHeight: 1.8, marginBottom: 0 }}>
            欢迎回来！登录你的账号，继续这段温暖的领养之旅。
          </p>
          <div style={{ display: 'flex', gap: '24px', marginTop: '48px', justifyContent: 'center' }}>
            {['🐱', '🐶', '🐰'].map((emoji) => (
              <div
                key={emoji}
                style={{
                  width: '72px',
                  height: '72px',
                  background: 'rgba(255,255,255,0.15)',
                  borderRadius: '20px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '36px',
                  backdropFilter: 'blur(8px)',
                }}
              >
                {emoji}
              </div>
            ))}
          </div>
          <div style={{ marginTop: '48px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
            {['AI 智能匹配，找到最合适的它', '海量待领养宠物，一键浏览', '全流程透明，进度实时追踪'].map((text) => (
              <div key={text} style={{ display: 'flex', alignItems: 'center', gap: '12px', color: '#fff', fontSize: '15px', textAlign: 'left' }}>
                <div
                  style={{
                    width: '32px',
                    height: '32px',
                    background: 'rgba(255,255,255,0.2)',
                    borderRadius: '10px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '16px',
                    flexShrink: 0,
                  }}
                >
                  ✓
                </div>
                <span>{text}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right Panel - Form */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', padding: '60px', background: '#FFF9F5' }}>
        <div style={{ width: '100%', maxWidth: '420px' }}>
          <div style={{ marginBottom: '40px' }}>
            <h2 style={{ fontSize: '36px', fontWeight: 700, color: '#4A4A4A', marginBottom: '8px' }}>登录账号</h2>
            <p style={{ fontSize: '15px', color: '#999' }}>
              还没有账号？
              <Link to="/register" style={{ color: '#FF8C42', textDecoration: 'none', fontWeight: 600 }}>
                立即注册
              </Link>
            </p>
          </div>

          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '24px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, color: '#4A4A4A', marginBottom: '10px' }}>用户名</label>
              <div style={{ position: 'relative' }}>
                <span style={{ position: 'absolute', left: '18px', top: '50%', transform: 'translateY(-50%)', fontSize: '18px', color: '#ccc' }}>👤</span>
                <input
                  type="text"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  placeholder="请输入用户名"
                  style={{
                    width: '100%',
                    padding: '16px 20px',
                    paddingLeft: '48px',
                    border: '2px solid #FFE5CC',
                    borderRadius: '14px',
                    fontSize: '15px',
                    fontFamily: 'inherit',
                    background: '#fff',
                    color: '#4A4A4A',
                    outline: 'none',
                    transition: 'all 0.3s',
                  }}
                />
              </div>
            </div>

            <div style={{ marginBottom: '24px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, color: '#4A4A4A', marginBottom: '10px' }}>密码</label>
              <div style={{ position: 'relative' }}>
                <span style={{ position: 'absolute', left: '18px', top: '50%', transform: 'translateY(-50%)', fontSize: '18px', color: '#ccc' }}>🔒</span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  placeholder="请输入密码"
                  style={{
                    width: '100%',
                    padding: '16px 20px',
                    paddingLeft: '48px',
                    border: '2px solid #FFE5CC',
                    borderRadius: '14px',
                    fontSize: '15px',
                    fontFamily: 'inherit',
                    background: '#fff',
                    color: '#4A4A4A',
                    outline: 'none',
                    transition: 'all 0.3s',
                  }}
                />
                <span
                  onClick={() => setShowPassword(!showPassword)}
                  style={{ position: 'absolute', right: '18px', top: '50%', transform: 'translateY(-50%)', cursor: 'pointer', fontSize: '18px', color: '#ccc', padding: '4px' }}
                >
                  {showPassword ? '🙈' : '👁️'}
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={formData.rememberMe}
                  onChange={(e) => setFormData({ ...formData, rememberMe: e.target.checked })}
                  style={{ width: '18px', height: '18px', accentColor: '#FF8C42', cursor: 'pointer' }}
                />
                <span style={{ fontSize: '14px', color: '#666' }}>记住我</span>
              </label>
              <Link to="#" style={{ fontSize: '14px', color: '#FF8C42', textDecoration: 'none', fontWeight: 500 }}>
                忘记密码？
              </Link>
            </div>

            <button
              type="submit"
              disabled={loading}
              style={{
                width: '100%',
                padding: '18px',
                background: loading ? '#FFE5CC' : 'linear-gradient(135deg, #FF8C42, #FF6B35)',
                border: 'none',
                borderRadius: '14px',
                color: '#fff',
                fontSize: '16px',
                fontWeight: 600,
                fontFamily: 'inherit',
                cursor: loading ? 'not-allowed' : 'pointer',
                boxShadow: loading ? 'none' : '0 8px 24px rgba(255,107,53,0.3)',
                transition: 'all 0.3s',
              }}
            >
              {loading ? '登录中...' : '登录'}
            </button>
          </form>

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', margin: '28px 0' }}>
            <div style={{ flex: 1, height: '1px', background: '#FFE5CC' }} />
            <div style={{ fontSize: '13px', color: '#ccc', fontWeight: 500 }}>或</div>
            <div style={{ flex: 1, height: '1px', background: '#FFE5CC' }} />
          </div>

          <div style={{ display: 'flex', gap: '16px' }}>
            <button
              type="button"
              style={{
                flex: 1,
                padding: '14px',
                border: '2px solid #FFE5CC',
                borderRadius: '14px',
                background: '#fff',
                fontSize: '14px',
                fontWeight: 600,
                fontFamily: 'inherit',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '10px',
                color: '#4A4A4A',
                transition: 'all 0.3s',
              }}
            >
              <span style={{ fontSize: '18px' }}>💬</span>
              <span>微信</span>
            </button>
            <button
              type="button"
              style={{
                flex: 1,
                padding: '14px',
                border: '2px solid #FFE5CC',
                borderRadius: '14px',
                background: '#fff',
                fontSize: '14px',
                fontWeight: 600,
                fontFamily: 'inherit',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '10px',
                color: '#4A4A4A',
                transition: 'all 0.3s',
              }}
            >
              <span style={{ fontSize: '18px' }}>🍎</span>
              <span>Apple</span>
            </button>
          </div>

          <div style={{ textAlign: 'center', marginTop: '32px', fontSize: '14px', color: '#999' }}>
            登录即表示同意
            <Link to="#" style={{ color: '#FF8C42', textDecoration: 'none' }}>
              《用户协议》
            </Link>
            和
            <Link to="#" style={{ color: '#FF8C42', textDecoration: 'none' }}>
              《隐私政策》
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
