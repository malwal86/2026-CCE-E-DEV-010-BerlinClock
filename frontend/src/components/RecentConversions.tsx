import type { Conversion } from '../api/types'
import { BerlinClock } from './BerlinClock'

interface Props {
  conversions: Conversion[]
}

export function RecentConversions({ conversions }: Props) {
  return (
    <section className="panel" aria-labelledby="recent-heading">
      <h2 id="recent-heading">Recent conversions</h2>
      {conversions.length === 0 ? (
        <p className="muted">No conversions yet</p>
      ) : (
        <ol className="recent-list">
          {conversions.map((conversion) => (
            <li key={conversion.id} className="recent-list__entry">
              <BerlinClock clock={conversion} size="small" />
              <span className="recent-list__time" data-testid="time">
                {conversion.time}
              </span>
              <time className="muted" dateTime={conversion.convertedAt}>
                {new Date(conversion.convertedAt).toLocaleString()}
              </time>
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}
