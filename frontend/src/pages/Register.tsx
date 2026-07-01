import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { message } from 'antd';
import { useAuthStore } from '../stores/authStore';
import { authApi } from '../api/auth';

export default function Register() {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    phone: '',
    email: '',
    role: 'ADOPTER',
    nickname: '',
    city: '',
    agree: false,
  });

  const handleRegister = async () => {
    if (!formData.username || !formData.password) {
      message.error('请填写用户名和密码');
      return;
    }
    if (formData.password.length < 6) {
      message.error('密码至少6位');
      return;
    }
    if (formData.password !== formData.confirmPassword) {
      message.error('两次密码不一致');
      return;
    }
    if (!formData.agree) {
      message.error('请同意用户协议');
      return;
    }

    setLoading(true);
    
    const response = await authApi.register({
      username: formData.username,
      password: formData.password,
      phone: formData.phone,
      email: formData.email,
      role: formData.role,
    }).catch((err) => {
      message.error(err.message || '注册失败');
      return null;
    });

    setLoading(false);

    if (response?.data) {
      login(response.data, response.data.token);
      setStep(3);
    }
  };

  const renderStepIndicator = () => (
    <div style={{ display: 'flex', justifyContent: 'center', gap: '12px', marginBottom: '32px' }}>
      {[1, 2, 3].map((s) => (
        <div key={s} style={{ display: 'flex', alignItems: 'center' }}>
          <div
            style={{
              width: '36px',
              height: '36px',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '14px',
              fontWeight: 600,
              background: step > s ? '#4CAF50' : step === s ? 'linear-gradient(135deg, #FF8C42, #FF6B35)' : '#FFE5CC',
              color: step >= s ? '#fff' : '#ccc',
              boxShadow: step === s ? '0 4px 12px rgba(255,107,53,0.3)' : 'none',
            }}
          >
            {step > s ? '✓' : s}
          </div>
          {s < 3 && (
            <div
              style={{
                width: '60px',
                height: '3px',
                background: step > s ? 'linear-gradient(90deg, #FF8C42, #FFB347)' : '#FFE5CC',
              }}
            />
          )}
        </div>
      ))}
    </div>
  );

  const renderStep1 = () => (
    <>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '36px', fontWeight: 700, color: '#4A4A4A', marginBottom: '8px' }}>创建账号</h2>
        <p style={{ fontSize: '15px', color: '#999' }}>
          已有账号？
          <Link to="/login" style={{ color: '#FF8C42', textDecoration: 'none', fontWeight: 600 }}>
            立即登录
          </Link>
        </p>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, color: '#4A4A4A', marginBottom: '8px' }}>用户名</label>
        <div style={{ position: 'relative' }}>
          <span style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', fontSize: '16px', color: '#ccc' }}>👤</span>
          <input
            type="text"
            value={formData.username}
            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
            placeholder="请输入用户名"
            style={{
              width: '100%',
              padding: '14px 18px',
              paddingLeft: '46px',
              border: '2px solid #FFE5CC',
              borderRadius: '12px',
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

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, color: '#4A4A4A', marginBottom: '8px' }}>手机号</label>
        <div style={{ position: 'relative' }}>
          <span style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', fontSize: '16px', color: '#ccc' }}>📱</span>
          <input
            type="tel"
            value={formData.phone}
            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            placeholder="请输入手机号"
            maxLength={11}
            style={{
              width: '100%',
              padding: '14px 18px',
              paddingLeft: '46px',
              border: '2px solid #FFE5CC',
              borderRadius: '12px',
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

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, color: '#4A4A4A', marginBottom: '8px' }}>邮箱</label>
        <div style={{ position: 'relative' }}>
          <span style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', fontSize: '16px', color: '#ccc' }}>📧</span>
          <input
            type="email"
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            placeholder="请输入邮箱"
            style={{
              width: '100%',
              padding: '14px 18px',
              paddingLeft: '46px',
              border: '2px solid #FFE5CC',
              borderRadius: '12px',
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

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, color: '#4A4A4A', marginBottom: '8px' }}>设置密码</label>
        <div style={{ position: 'relative' }}>
          <span style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', fontSize: '16px', color: '#ccc' }}>🔒</span>
          <input
            type={showPassword ? 'text' : 'password'}
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            placeholder="请设置 6 位以上密码"
            style={{
              width: '100%',
              padding: '14px 18px',
              paddingLeft: '46px',
              border: '2px solid #FFE5CC',
              borderRadius: '12px',
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
            style={{ position: 'absolute', right: '16px', top: '50%', transform: 'translateY(-50%)', cursor: 'pointer', fontSize: '16px', color: '#ccc' }}
          >
            {showPassword ? '🙈' : '👁️'}
          </span>
        </div>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, color: '#4A4A4A', marginBottom: '8px' }}>确认密码</label>
        <div style={{ position: 'relative' }}>
          <span style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', fontSize: '16px', color: '#ccc' }}>🔒</span>
          <input
            type="password"
            value={formData.confirmPassword}
            onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
            placeholder="请再次输入密码"
            style={{
              width: '100%',
              padding: '14px 18px',
              paddingLeft: '46px',
              border: '2px solid #FFE5CC',
              borderRadius: '12px',
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

      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', marginBottom: '24px' }}>
        <input
          type="checkbox"
          checked={formData.agree}
          onChange={(e) => setFormData({ ...formData, agree: e.target.checked })}
          style={{ width: '18px', height: '18px', accentColor: '#FF8C42', cursor: 'pointer', marginTop: '2px', flexShrink: 0 }}
        />
        <span style={{ fontSize: '13px', color: '#666', lineHeight: 1.6 }}>
          我已阅读并同意
          <Link to="#" style={{ color: '#FF8C42', textDecoration: 'none' }}>
            《用户协议》
          </Link>
          和
          <Link to="#" style={{ color: '#FF8C42', textDecoration: 'none' }}>
            《隐私政策》
          </Link>
        </span>
      </div>

      <button
        type="button"
        onClick={() => setStep(2)}
        style={{
          width: '100%',
          padding: '16px',
          background: 'linear-gradient(135deg, #FF8C42, #FF6B35)',
          border: 'none',
          borderRadius: '14px',
          color: '#fff',
          fontSize: '16px',
          fontWeight: 600,
          fontFamily: 'inherit',
          cursor: 'pointer',
          boxShadow: '0 8px 24px rgba(255,107,53,0.3)',
          transition: 'all 0.3s',
        }}
      >
        下一步，选择身份
      </button>
    </>
  );

  const renderStep2 = () => (
    <>
      <div style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '36px', fontWeight: 700, color: '#4A4A4A', marginBottom: '8px' }}>选择身份</h2>
        <p style={{ fontSize: '15px', color: '#999' }}>告诉我们你的身份，我们将为你提供个性化服务</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '24px' }}>
        <div
          onClick={() => setFormData({ ...formData, role: 'ADOPTER' })}
          style={{
            border: formData.role === 'ADOPTER' ? '2px solid #FF8C42' : '2px solid #FFE5CC',
            borderRadius: '14px',
            padding: '20px',
            textAlign: 'center',
            cursor: 'pointer',
            transition: 'all 0.3s',
            background: formData.role === 'ADOPTER' ? '#FFF9F5' : '#fff',
            boxShadow: formData.role === 'ADOPTER' ? '0 4px 16px rgba(255,140,66,0.15)' : 'none',
          }}
        >
          <div style={{ fontSize: '36px', marginBottom: '8px' }}>🏠</div>
          <h4 style={{ fontSize: '15px', color: '#4A4A4A', marginBottom: '4px' }}>领养人</h4>
          <p style={{ fontSize: '12px', color: '#999' }}>我想领养宠物</p>
        </div>
        <div
          onClick={() => setFormData({ ...formData, role: 'SHELTER' })}
          style={{
            border: formData.role === 'SHELTER' ? '2px solid #FF8C42' : '2px solid #FFE5CC',
            borderRadius: '14px',
            padding: '20px',
            textAlign: 'center',
            cursor: 'pointer',
            transition: 'all 0.3s',
            background: formData.role === 'SHELTER' ? '#FFF9F5' : '#fff',
            boxShadow: formData.role === 'SHELTER' ? '0 4px 16px rgba(255,140,66,0.15)' : 'none',
          }}
        >
          <div style={{ fontSize: '36px', marginBottom: '8px' }}>🏥</div>
          <h4 style={{ fontSize: '15px', color: '#4A4A4A', marginBottom: '4px' }}>救助站</h4>
          <p style={{ fontSize: '12px', color: '#999' }}>我是机构/救助组织</p>
        </div>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, color: '#4A4A4A', marginBottom: '8px' }}>昵称</label>
        <div style={{ position: 'relative' }}>
          <span style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', fontSize: '16px', color: '#ccc' }}>✨</span>
          <input
            type="text"
            value={formData.nickname}
            onChange={(e) => setFormData({ ...formData, nickname: e.target.value })}
            placeholder="给自己起个昵称"
            style={{
              width: '100%',
              padding: '14px 18px',
              paddingLeft: '46px',
              border: '2px solid #FFE5CC',
              borderRadius: '12px',
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
        <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, color: '#4A4A4A', marginBottom: '8px' }}>所在城市</label>
        <div style={{ position: 'relative' }}>
          <span style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', fontSize: '16px', color: '#ccc' }}>📍</span>
          <input
            type="text"
            value={formData.city}
            onChange={(e) => setFormData({ ...formData, city: e.target.value })}
            placeholder="如：上海 / 深圳 / 杭州"
            style={{
              width: '100%',
              padding: '14px 18px',
              paddingLeft: '46px',
              border: '2px solid #FFE5CC',
              borderRadius: '12px',
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

      <button
        type="button"
        onClick={handleRegister}
        disabled={loading}
        style={{
          width: '100%',
          padding: '16px',
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
        {loading ? '注册中...' : '完成注册'}
      </button>

      <button
        type="button"
        onClick={() => setStep(1)}
        style={{
          width: '100%',
          padding: '14px',
          background: 'transparent',
          border: '2px solid #FFE5CC',
          borderRadius: '14px',
          color: '#666',
          fontSize: '15px',
          fontWeight: 500,
          fontFamily: 'inherit',
          cursor: 'pointer',
          marginTop: '12px',
          transition: 'all 0.3s',
        }}
      >
        返回上一步
      </button>
    </>
  );

  const renderStep3 = () => (
    <div style={{ textAlign: 'center', padding: '40px 20px' }}>
      <div
        style={{
          width: '80px',
          height: '80px',
          background: 'linear-gradient(135deg, #4CAF50, #66BB6A)',
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '40px',
          margin: '0 auto 20px',
          boxShadow: '0 8px 24px rgba(76,175,80,0.3)',
        }}
      >
        ✓
      </div>
      <h3 style={{ fontSize: '24px', color: '#4A4A4A', marginBottom: '8px' }}>注册成功！</h3>
      <p style={{ fontSize: '14px', color: '#999', marginBottom: '24px' }}>
        欢迎加入 PawMatch 爱心社区
        <br />
        现在去探索你的第一位"命中注定"吧 🐾
      </p>
      <button
        onClick={() => navigate('/')}
        style={{
          display: 'inline-block',
          padding: '14px 36px',
          background: 'linear-gradient(135deg, #FF8C42, #FF6B35)',
          border: 'none',
          borderRadius: '50px',
          color: '#fff',
          textDecoration: 'none',
          fontWeight: 600,
          fontSize: '15px',
          boxShadow: '0 8px 24px rgba(255,107,53,0.3)',
          cursor: 'pointer',
        }}
      >
        开始探索 🐾
      </button>
    </div>
  );

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
            成为
            <br />
            爱心社区的一员
          </h1>
          <p style={{ fontSize: '18px', color: 'rgba(255,255,255,0.9)', lineHeight: 1.8, marginBottom: 0 }}>
            注册 PawMatch，开启你的领养之旅，为流浪动物带来希望。
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
          <div style={{ marginTop: '48px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '20px', width: '100%' }}>
            {['填写手机号，创建账号', '完善个人信息，生成你的专属画像', '浏览宠物，AI 帮你找到最 match 的它'].map((text, index) => (
              <div key={text} style={{ display: 'flex', alignItems: 'flex-start', gap: '14px', width: '100%', maxWidth: '320px' }}>
                <div
                  style={{
                    width: '32px',
                    height: '32px',
                    background: 'rgba(255,255,255,0.25)',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#fff',
                    fontWeight: 700,
                    fontSize: '14px',
                    flexShrink: 0,
                  }}
                >
                  {index + 1}
                </div>
                <div style={{ color: '#fff', fontSize: '15px', lineHeight: 1.6, paddingTop: '4px', textAlign: 'left', flex: 1 }}>{text}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right Panel - Form */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', padding: '40px 60px', background: '#FFF9F5', overflowY: 'auto' }}>
        <div style={{ width: '100%', maxWidth: '460px' }}>
          {step !== 3 && renderStepIndicator()}
          {step === 1 && renderStep1()}
          {step === 2 && renderStep2()}
          {step === 3 && renderStep3()}
        </div>
      </div>
    </div>
  );
}
