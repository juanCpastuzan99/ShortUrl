import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import Home from './pages/Home.jsx'
import Report from './pages/Report.jsx'

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/report" element={<Report />} />
      </Routes>
      <footer className="text-center text-secondary py-3 border-top border-secondary mt-5">
        <small>Acortador de Enlaces — Spring Boot · Arquitectura Hexagonal · React</small>
      </footer>
    </>
  )
}
