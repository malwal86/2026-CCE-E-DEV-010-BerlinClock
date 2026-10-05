import { useState, type FormEvent } from 'react'
import './ConvertForm.css'

interface Props {
  onConvert: (time: string) => void
}

export function ConvertForm({ onConvert }: Props) {
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
          value={time}
          onChange={(event) => setTime(event.target.value)}
        />
        <button type="submit">Convert</button>
      </div>
    </form>
  )
}
