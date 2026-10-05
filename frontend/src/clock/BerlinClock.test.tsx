import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { BerlinClock } from './BerlinClock'

describe('BerlinClock', () => {
  it('lights the seconds lamp yellow on an even second', () => {
    render(<BerlinClock clock={{ seconds: 'Y' }} />)

    expect(screen.getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'Y')
  })

  it('turns the seconds lamp off on an odd second', () => {
    render(<BerlinClock clock={{ seconds: 'O' }} />)

    expect(screen.getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'O')
  })
})
