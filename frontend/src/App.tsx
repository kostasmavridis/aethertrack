import { Routes, Route, NavLink } from 'react-router-dom'
import RegimensPage from './pages/RegimensPage'
import NewRegimenPage from './pages/NewRegimenPage'
import './App.css'

export default function App() {
  return (
    <div className="app">
      <nav className="nav">
        <NavLink to="/" end className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Regimens
        </NavLink>
        <NavLink to="/regimens/new" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          + New Regimen
        </NavLink>
      </nav>
      <main className="main">
        <Routes>
          <Route path="/" element={<RegimensPage />} />
          <Route path="/regimens/new" element={<NewRegimenPage />} />
        </Routes>
      </main>
    </div>
  )
}
