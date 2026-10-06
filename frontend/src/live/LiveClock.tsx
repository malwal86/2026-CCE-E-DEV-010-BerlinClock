import { BerlinClock } from '../clock/BerlinClock'
import { ClockCode } from '../clock/ClockCode'
import { useLiveBerlinClock } from './useLiveBerlinClock'
import './LiveClock.css'

/**
 * The current time as a Berlin Clock, ticking every second. When the backend cannot be reached, a banner says so and
 * the last known time stays, dimmed, until it answers again.
 */
export function LiveClock() {
  const { reading, unavailable } = useLiveBerlinClock()

  return (
    <>
      {unavailable && (
        <p className="banner" role="alert">
          Backend unavailable. Retrying…
        </p>
      )}
      <section className={unavailable ? 'panel live live--stale' : 'panel live'} aria-labelledby="live-heading">
        <h2 id="live-heading">Live</h2>
        {reading ? (
          <>
            <BerlinClock clock={reading} time={reading.time} />
            <p className="live__time">
              <time>{reading.time}</time>
            </p>
            <ClockCode code={reading.clock} time={reading.time} />
          </>
        ) : (
          <p className="muted">Starting the clock…</p>
        )}
      </section>
    </>
  )
}
