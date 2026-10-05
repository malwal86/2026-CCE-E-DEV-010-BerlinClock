import { useEffect, useState } from 'react'
import { convertTime, fetchRecentConversions } from './api/conversions'
import type { Conversion } from './api/types'
import { BerlinClock } from './components/BerlinClock'
import { ConvertForm } from './components/ConvertForm'
import { RecentConversions } from './components/RecentConversions'
import './App.css'

function App() {
  const [result, setResult] = useState<Conversion>()
  const [recent, setRecent] = useState<Conversion[]>([])

  useEffect(() => {
    fetchRecentConversions().then(setRecent)
  }, [])

  async function convert(time: string) {
    setResult(await convertTime(time))
    setRecent(await fetchRecentConversions())
  }

  return (
    <main className="app">
      <h1>Berlin Clock</h1>
      <section className="panel" aria-label="Convert a time">
        <ConvertForm onConvert={convert} />
      </section>
      {result && (
        <section className="panel result" aria-labelledby="result-heading">
          <h2 id="result-heading">Result</h2>
          <BerlinClock clock={result} />
          <p className="result__time">{result.time}</p>
        </section>
      )}
      <RecentConversions conversions={recent} />
    </main>
  )
}

export default App
