import { BrowserRouter, Route, Routes } from 'react-router-dom'

const Home = () => (
  <main style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
    <h1>AetherTrack 🌿</h1>
    <p>Intelligent supplement scheduling – frontend scaffold ready.</p>
    <ul>
      <li>Slice 15 → Regimen Builder</li>
      <li>Slice 16 → Schedule View</li>
      <li>Slice 17 → Intake Logging</li>
    </ul>
  </main>
)

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
      </Routes>
    </BrowserRouter>
  )
}
