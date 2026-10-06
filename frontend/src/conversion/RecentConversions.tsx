import type { MouseEvent } from 'react'
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
}

export function RecentConversions({ conversions, onOpen, openId }: Props) {
  function open(event: MouseEvent<HTMLAnchorElement>, id: number) {
    // A middle click or Cmd/Ctrl/Shift/Alt click is the browser's: a new tab or window on the same link.
    if (event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return
    event.preventDefault()
    onOpen(id)
  }

  return (
    <section className="panel" aria-labelledby="recent-heading">
      <h2 id="recent-heading">Recent conversions</h2>
      {conversions.length === 0 ? (
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
