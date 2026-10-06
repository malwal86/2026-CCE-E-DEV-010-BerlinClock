import { useCallback, useEffect, useState } from 'react'
import { ApiProblem, clearHistory, convertTime, fetchConversion, fetchRecentConversions } from './conversion/api'
import { conversionIdIn, conversionPath } from './conversion/route'
import type { Conversion } from './conversion/types'
import { BerlinClock } from './clock/BerlinClock'
import { ClockCode } from './clock/ClockCode'
import { ConvertForm } from './conversion/ConvertForm'
import { RecentConversions } from './conversion/RecentConversions'
import { LiveClock } from './live/LiveClock'
import './App.css'

/** Puts a page in the address bar, so the result shown can be shared, reloaded and reached with Back. */
function navigate(path: string) {
  if (window.location.pathname !== path) {
    window.history.pushState(null, '', path)
  }
}

function App() {
  const [result, setResult] = useState<Conversion>()
  /** Why the conversion in the address bar cannot be shown. */
  const [lookupError, setLookupError] = useState<string>()
  const [recent, setRecent] = useState<Conversion[]>([])
  const [error, setError] = useState<string>()

  const show = useCallback(async (id: number | undefined) => {
    if (id === undefined) {
      setResult(undefined)
      setLookupError(undefined)
      return
    }
    try {
      setResult(await fetchConversion(id))
      setLookupError(undefined)
    } catch (problem) {
      if (!(problem instanceof ApiProblem)) throw problem
      setResult(undefined)
      setLookupError(problem.message)
    }
  }, [])

  useEffect(() => {
    fetchRecentConversions().then(setRecent)
  }, [])

  useEffect(() => {
    const showAddress = () => show(conversionIdIn(window.location.pathname))
    showAddress()
    window.addEventListener('popstate', showAddress)
    return () => window.removeEventListener('popstate', showAddress)
  }, [show])

  async function convert(time: string) {
    try {
      const conversion = await convertTime(time)
      setResult(conversion)
      setLookupError(undefined)
      setError(undefined)
      navigate(conversionPath(conversion.id))
    } catch (problem) {
      if (!(problem instanceof ApiProblem)) throw problem
      setResult(undefined)
      setLookupError(undefined)
      setError(problem.message)
      navigate('/')
      return
    }
    setRecent(await fetchRecentConversions())
  }

  async function clear() {
    await clearHistory()
    setRecent([])
    // The conversion in the address bar is gone too: say so rather than show a deleted one.
    if (result) await show(result.id)
  }

  function open(id: number) {
    navigate(conversionPath(id))
    show(id)
  }

  return (
    <main className="app">
      <h1>Berlin Clock</h1>
      <LiveClock />
      <section className="panel" aria-label="Convert a time">
        <ConvertForm onConvert={convert} error={error} />
      </section>
      {(result || lookupError) && (
        <section className="panel result" aria-labelledby="result-heading">
          <h2 id="result-heading">Result</h2>
          {result ? (
            <>
              <BerlinClock clock={result} time={result.time} />
              <p className="result__time">{result.time}</p>
              <ClockCode code={result.clock} time={result.time} />
            </>
          ) : (
            <p className="result__error" role="alert">
              {lookupError}
            </p>
          )}
        </section>
      )}
      <RecentConversions conversions={recent} onOpen={open} openId={result?.id} onClear={clear} />
    </main>
  )
}

export default App
