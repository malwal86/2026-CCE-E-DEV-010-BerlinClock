import type { BerlinClockRows } from '../clock/types'

/** A Berlin Clock reading of a time, not saved anywhere. */
export interface BerlinClockReading extends BerlinClockRows {
  time: string
}
