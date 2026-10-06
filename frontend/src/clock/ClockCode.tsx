import { useEffect, useState } from 'react'
import './ClockCode.css'

interface Props {
  /** The entire clock as its 24-character kata code. */
  code: string
  /** The time the code shows, to tell one copy button from another. */
  time: string
}

const COPY = 'Copy'

/** The clock as text, readable without seeing colours, with a button to copy it. */
export function ClockCode({ code, time }: Props) {
  const [action, setAction] = useState(COPY)

  useEffect(() => {
    if (action === COPY) return
    const timer = setTimeout(() => setAction(COPY), 2000)
    return () => clearTimeout(timer)
  }, [action])

  async function copy() {
    try {
      await navigator.clipboard.writeText(code)
      setAction('Copied')
    } catch {
      setAction("Couldn't copy")
    }
  }

  return (
    <div className="clock-code">
      <code className="clock-code__text">{code}</code>
      <button type="button" className="clock-code__copy" aria-label={`${action} code for ${time}`} onClick={copy}>
        {action}
      </button>
    </div>
  )
}
