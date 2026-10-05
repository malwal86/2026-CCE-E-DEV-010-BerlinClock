import { useEffect, useState } from 'react'
import { ApiProblem, convertTime, fetchRecentConversions } from './conversion/api'
import type { Conversion } from './conversion/types'
import { BerlinClock } from './clock/BerlinClock'
import { ConvertForm } from './conversion/ConvertForm'
import { RecentConversions } from './conversion/RecentConversions'
import './App.css'

function App() {
  const [result, setResult] = useState<Conversion>()
  const [recent, setRecent] = useState<Conversion[]>([])
  const [error, setError] = useState<string>()

  useEffect(() => {
    fetchRecentConversions().then(setRecent)
  }, [])

  async function convert(time: string) {
    try {
      setResult(await convertTime(time))
      setError(undefined)
    } catch (problem) {
      if (!(problem instanceof ApiProblem)) throw problem
      setResult(undefined)
      setError(problem.message)
      return
    }
    setRecent(await fetchRecentConversions())
  }

  return (
    <main className="app">
      <h1>Berlin Clock</h1>
      <section className="panel" aria-label="Convert a time">
        <ConvertForm onConvert={convert} error={error} />
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
