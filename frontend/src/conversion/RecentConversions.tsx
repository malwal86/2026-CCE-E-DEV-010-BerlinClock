import { useEffect, useRef, useState, type MouseEvent } from 'react'
import type { Conversion } from './types'
import { conversionPath } from './route'
import { BerlinClock } from '../clock/BerlinClock'
import { ClockCode } from '../clock/ClockCode'
import './RecentConversions.css'

interface Props {
  conversions: Conversion[]
  /** Called when an entry is clicked, to show it without leaving the page. */
  onOpen: (id: number) => void
  /** The conversion shown in the result panel, marked as the current page. */
  openId?: number
  /** Called once clearing is confirmed. */
  onClear: () => Promise<void>
  /** The history could not be read: `conversions` says nothing about what it holds. */
  unavailable?: boolean
}

const QUESTION = 'Delete all conversions? This cannot be undone.'

export function RecentConversions({ conversions, onOpen, openId, onClear, unavailable = false }: Props) {
  const [confirming, setConfirming] = useState(false)
  const clearButton = useRef<HTMLButtonElement>(null)
  const cancelButton = useRef<HTMLButtonElement>(null)

  // Keyboard and screen reader users land on the safe choice, and come back to where they were on Cancel.
  useEffect(() => {
    if (confirming) cancelButton.current?.focus()
  }, [confirming])

  function cancel() {
    setConfirming(false)
    clearButton.current?.focus()
  }

  async function confirm() {
    await onClear()
    setConfirming(false)
  }

  function open(event: MouseEvent<HTMLAnchorElement>, id: number) {
    // A middle click or Cmd/Ctrl/Shift/Alt click is the browser's: a new tab or window on the same link.
    if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return
    event.preventDefault()
    onOpen(id)
  }

  return (
    <section className="panel" aria-labelledby="recent-heading">
      <div className="recent-header">
        <h2 id="recent-heading">Recent conversions</h2>
        <button
          ref={clearButton}
          type="button"
          className="recent-header__clear"
          disabled={unavailable || conversions.length === 0}
          onClick={() => setConfirming(true)}
        >
          Clear history
        </button>
      </div>
      {confirming && (
        <div className="clear-confirmation" role="group" aria-labelledby="clear-question">
          <p id="clear-question">{QUESTION}</p>
          <div className="clear-confirmation__actions">
            <button type="button" className="clear-confirmation__delete" onClick={confirm}>
              Delete
            </button>
            <button ref={cancelButton} type="button" onClick={cancel}>
              Cancel
            </button>
          </div>
        </div>
      )}
      {unavailable ? (
        <p className="muted">History unavailable</p>
      ) : conversions.length === 0 ? (
        <p className="muted">No conversions yet</p>
      ) : (
        <ol className="recent-list">
          {conversions.map((conversion) => (
            <li key={conversion.id} className="recent-list__entry">
              <BerlinClock clock={conversion} time={conversion.time} size="small" />
              <div className="recent-list__details">
                <p className="recent-list__when">
                  <a
                    className="recent-list__time"
                    data-testid="time"
                    href={conversionPath(conversion.id)}
                    aria-current={conversion.id === openId ? 'page' : undefined}
                    onClick={(event) => open(event, conversion.id)}
                  >
                    {conversion.time}
                  </a>
                  <time className="muted" dateTime={conversion.convertedAt}>
                    {new Date(conversion.convertedAt).toLocaleString()}
                  </time>
                </p>
                <ClockCode code={conversion.clock} time={conversion.time} />
              </div>
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}
