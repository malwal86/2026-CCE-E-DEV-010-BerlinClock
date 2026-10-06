import './LampRow.css'

interface Props {
  /** The row in the kata notation, one lamp per character, left to right (e.g. "RRRO"). */
  lamps: string
  testId: string
}

/** A row of rectangular lamps, reused by every hour and minute row. */
export function LampRow({ lamps, testId }: Props) {
  return (
    <div className="lamp-row" data-testid={testId}>
      {[...lamps].map((lamp, position) => (
        <span key={position} className="lamp-row__lamp" data-lamp={lamp} />
      ))}
    </div>
  )
}
