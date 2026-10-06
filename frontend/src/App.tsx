import { useCallback, useEffect, useState } from 'react'
import { ApiProblem, clearHistory, convertTime, fetchConversion, fetchRecentConversions, rethrowUnlessApiProblem } from './conversion/api'
import { conversionIdIn, conversionPath } from './conversion/route'
import type { Conversion } from './conversion/types'
import { BerlinClock } from './clock/BerlinClock'
import { ClockCode } from './clock/ClockCode'
import { ConvertForm } from './conversion/ConvertForm'
import { RecentConversions } from './conversion/RecentConversions'
import { LiveClock } from './live/LiveClock'
import { useLatestRequest } from './useLatestRequest'
import './App.css'

/** Puts a page in the address bar, so the result shown can be shared, reloaded and reached with Back. */
function openAddress(path: string) {
  if (window.location.pathname !== path) window.history.pushState(null, '', path)
}

function replaceAddress(path: string) {
  if (window.location.pathname !== path) window.history.replaceState(null, '', path)
}

/** Undefined when the history cannot be read (the database or the backend is down). */
async function readRecentConversions(): Promise<Conversion[] | undefined> {
  try {
    return await fetchRecentConversions()
  } catch (problem) {
    rethrowUnlessApiProblem(problem)
    return undefined
  }
}

/** A conversion, or why the one in the address bar cannot be shown. */
type Result = { conversion: Conversion; problem?: never } | { conversion?: never; problem: string }

function App() {
  const [result, setResult] = useState<Result>()
  const [recent, setRecent] = useState<Conversion[] | undefined>([])
  const [convertProblem, setConvertProblem] = useState<ApiProblem>()
  const startRequest = useLatestRequest()

  const refreshRecent = useCallback(async () => setRecent(await readRecentConversions()), [])

  const show = useCallback(
    async (id: number | undefined) => {
      const isStillLatest = startRequest()
      if (id === undefined) return setResult(undefined)
      try {
        const conversion = await fetchConversion(id)
        if (isStillLatest()) setResult({ conversion })
      } catch (problem) {
        rethrowUnlessApiProblem(problem)
        if (isStillLatest()) setResult({ problem: problem.message })
      }
    },
    [startRequest],
  )

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
    const isStillLatest = startRequest()
    try {
      const conversion = await convertTime(time)
      setConvertProblem(undefined)
      if (isStillLatest()) {
        setResult({ conversion })
        openAddress(conversionPath(conversion.id))
      }
    } catch (problem) {
      rethrowUnlessApiProblem(problem)
      setConvertProblem(problem)
      if (isStillLatest()) {
        setResult(undefined)
        openAddress('/')
      }
    }
    await refreshRecent()
  }

  async function clear() {
    try {
      await clearHistory()
    } catch (problem) {
      rethrowUnlessApiProblem(problem)
      return refreshRecent()
    }
    setRecent([])
    // The conversion shown is gone too: close it rather than say "not found", without adding a Back step.
    replaceAddress('/')
    show(undefined)
  }

  function open(id: number) {
    openAddress(conversionPath(id))
    show(id)
  }

  return (
    <main className="app">
      <h1>Berlin Clock</h1>
      <LiveClock />
      <section className="panel" aria-label="Convert a time">
        <ConvertForm onConvert={convert} problem={convertProblem} />
      </section>
      {result && (
        <section className="panel result" aria-labelledby="result-heading">
          <h2 id="result-heading">Result</h2>
          {result.conversion ? (
            <>
              <BerlinClock clock={result.conversion} time={result.conversion.time} />
              <p className="result__time">{result.conversion.time}</p>
              <ClockCode code={result.conversion.clock} time={result.conversion.time} />
            </>
          ) : (
            <p className="result__error" role="alert">
              {result.problem}
            </p>
          )}
        </section>
      )}
      <RecentConversions
        conversions={recent ?? []}
        unavailable={recent === undefined}
        onOpen={open}
        openId={result?.conversion?.id}
        onClear={clear}
      />
    </main>
  )
}

export default App
