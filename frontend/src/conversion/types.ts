import type { BerlinClockRows } from '../clock/types'

export interface Conversion extends BerlinClockRows {
  id: number
  time: string
  convertedAt: string
}
