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

/** The recent conversions, or undefined when the history cannot be read (the database or the backend is down). */
async function readRecentConversions(): Promise<Conversion[] | undefined> {
  try {
    return await fetchRecentConversions()
  } catch (problem) {
    if (!(problem instanceof ApiProblem)) throw problem
    return undefined
  }
}

function App() {
  const [result, setResult] = useState<Conversion>()
  /** Why the conversion in the address bar cannot be shown. */
  const [lookupError, setLookupError] = useState<string>()
  const [recent, setRecent] = useState<Conversion[] | undefined>([])
  /** Why the time typed was rejected. */
  const [error, setError] = useState<string>()
  /** Why a conversion failed although the time was fine. */
  const [failure, setFailure] = useState<string>()

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
    readRecentConversions().then(setRecent)
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
      setFailure(undefined)
      navigate(conversionPath(conversion.id))
    } catch (problem) {
      if (!(problem instanceof ApiProblem)) throw problem
      setResult(undefined)
      setLookupError(undefined)
      navigate('/')
      if (problem.status === 400) {
        setError(problem.message)
        setFailure(undefined)
        return
      }
      setError(undefined)
      setFailure(problem.message)
    }
    setRecent(await readRecentConversions())
  }

  async function clear() {
    try {
      await clearHistory()
    } catch (problem) {
      if (!(problem instanceof ApiProblem)) throw problem
      setRecent(await readRecentConversions())
      return
    }
    setRecent([])
    // The conversion shown is gone too, and the reader knows it: close it rather than say "not found".
    // Replacing the address adds no Back step; an old link to it still says "not found".
    if (window.location.pathname !== '/') window.history.replaceState(null, '', '/')
    show(undefined)
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
        <ConvertForm onConvert={convert} error={error} failure={failure} />
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
      <RecentConversions
        conversions={recent ?? []}
        unavailable={recent === undefined}
        onOpen={open}
        openId={result?.id}
        onClear={clear}
      />
    </main>
  )
}

export default App
