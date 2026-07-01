import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import PetList from './pages/PetList'
import PetDetail from './pages/PetDetail'
import MyProfile from './pages/MyProfile'
import MyApplications from './pages/MyApplications'
import Chat from './pages/Chat'
import Login from './pages/Login'
import Register from './pages/Register'
import ShelterDashboard from './pages/ShelterDashboard'
import AboutUs from './pages/AboutUs'
import FollowUpManagement from './pages/FollowUpManagement'
import StatsDashboard from './pages/StatsDashboard'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="pets" element={<PetList />} />
        <Route path="pets/:id" element={<PetDetail />} />
        <Route path="profile" element={<MyProfile />} />
        <Route path="applications" element={<MyApplications />} />
        <Route path="chat" element={<Chat />} />
        <Route path="shelter" element={<ShelterDashboard />} />
        <Route path="follow-ups" element={<FollowUpManagement />} />
        <Route path="stats" element={<StatsDashboard />} />
        <Route path="about" element={<AboutUs />} />
      </Route>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
    </Routes>
  )
}

export default App
