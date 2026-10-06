import { useState, type FormEvent } from 'react'
import type { ApiProblem } from './api'
import './ConvertForm.css'

interface Props {
  onConvert: (time: string) => Promise<void>
  /** Why the last conversion failed: the time typed was rejected (400), or the backend or its database is down. */
  problem?: ApiProblem
}

export function ConvertForm({ onConvert, problem }: Props) {
  const [time, setTime] = useState('')
  const [converting, setConverting] = useState(false)
  const timeRejected = problem?.status === 400

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (converting) return
    setConverting(true)
    try {
      await onConvert(time.trim())
    } finally {
      setConverting(false)
    }
  }

  return (
    <form className="convert-form" onSubmit={submit}>
      <label htmlFor="time">Time (HH:mm:ss)</label>
      <div className="convert-form__row">
        <input
          id="time"
          name="time"
          placeholder="HH:mm:ss"
          autoComplete="off"
          aria-invalid={timeRejected || undefined}
          aria-describedby={timeRejected ? 'convert-problem' : undefined}
          value={time}
          onChange={(event) => setTime(event.target.value)}
        />
        <button type="submit" disabled={converting}>
          Convert
        </button>
      </div>
      {problem && (
        <p id="convert-problem" className="convert-form__error" role="alert">
          {problem.message}
        </p>
      )}
    </form>
  )
}
