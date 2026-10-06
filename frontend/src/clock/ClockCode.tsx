import { useEffect, useState } from 'react'
import './ClockCode.css'

interface Props {
  /** The entire clock as its 24-character kata code. */
  code: string
  /** The time the code shows, to tell one copy button from another. */
  time: string
}

/** The clock as text, readable without seeing colours, with a button to copy it. */
export function ClockCode({ code, time }: Props) {
  const [copied, setCopied] = useState(false)

  useEffect(() => {
    if (!copied) return
    const timer = setTimeout(() => setCopied(false), 2000)
    return () => clearTimeout(timer)
  }, [copied])

  async function copy() {
    await navigator.clipboard.writeText(code)
    setCopied(true)
  }

  const action = copied ? 'Copied' : 'Copy'
  return (
    <div className="clock-code">
      <code className="clock-code__text">{code}</code>
      <button type="button" className="clock-code__copy" aria-label={`${action} code for ${time}`} onClick={copy}>
        {action}
      </button>
    </div>
  )
}
