import { LampRow } from './LampRow'
import type { BerlinClockRows } from './types'
import './BerlinClock.css'

interface Props {
  clock: BerlinClockRows
  size?: 'large' | 'small'
}

export function BerlinClock({ clock, size = 'large' }: Props) {
  return (
    <div className={`berlin-clock berlin-clock--${size}`}>
      <span className="berlin-clock__seconds" data-testid="seconds-lamp" data-lamp={clock.seconds} />
      <LampRow lamps={clock.fiveHours} testId="five-hours-row" />
    </div>
  )
}
