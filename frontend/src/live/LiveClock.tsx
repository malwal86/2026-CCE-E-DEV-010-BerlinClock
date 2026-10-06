import { BerlinClock } from '../clock/BerlinClock'
import { ClockCode } from '../clock/ClockCode'
import { useLiveBerlinClock } from './useLiveBerlinClock'
import './LiveClock.css'

/** The current time as a Berlin Clock, ticking every second. */
export function LiveClock() {
  const reading = useLiveBerlinClock()

  return (
    <section className="panel live" aria-labelledby="live-heading">
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
  )
}
