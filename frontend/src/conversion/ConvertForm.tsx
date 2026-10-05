import { useState, type FormEvent } from 'react'
import './ConvertForm.css'

interface Props {
  onConvert: (time: string) => void
  /** Why the last time was rejected, shown next to the input. */
  error?: string
}

export function ConvertForm({ onConvert, error }: Props) {
  const [time, setTime] = useState('')

  function submit(event: FormEvent) {
    event.preventDefault()
    onConvert(time)
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
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? 'time-error' : undefined}
          value={time}
          onChange={(event) => setTime(event.target.value)}
        />
        <button type="submit">Convert</button>
      </div>
      {error && (
        <p id="time-error" className="convert-form__error" role="alert">
          {error}
        </p>
      )}
    </form>
  )
}
