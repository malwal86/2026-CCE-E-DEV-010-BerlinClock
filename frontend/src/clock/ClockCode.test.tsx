import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ClockCode } from './ClockCode'

describe('ClockCode', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('shows the 24-character code as text', () => {
    render(<ClockCode code="YRRROROOOYYRYYRYYRYOOOOO" time="16:50:06" />)

    expect(screen.getByText('YRRROROOOYYRYYRYYRYOOOOO')).toBeInTheDocument()
  })

  it('copies the code to the clipboard', async () => {
    const user = userEvent.setup()
    render(<ClockCode code="YRRROROOOYYRYYRYYRYOOOOO" time="16:50:06" />)

    await user.click(screen.getByRole('button', { name: 'Copy code for 16:50:06' }))

    expect(await navigator.clipboard.readText()).toBe('YRRROROOOYYRYYRYYRYOOOOO')
    expect(screen.getByRole('button', { name: 'Copied code for 16:50:06' })).toBeInTheDocument()
  })

  it('offers to copy again a moment later', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
    render(<ClockCode code="YRRROROOOYYRYYRYYRYOOOOO" time="16:50:06" />)
    await user.click(screen.getByRole('button', { name: 'Copy code for 16:50:06' }))
    await screen.findByRole('button', { name: 'Copied code for 16:50:06' })

    await act(() => vi.advanceTimersByTimeAsync(2000))

    // The 2-second reset is set by an effect after "Copied" renders, which a slow runner may not have run yet when
    // the clock is advanced. The fake clock also moves with real time, so waiting longer than the reset always ends it.
    expect(await screen.findByRole('button', { name: 'Copy code for 16:50:06' }, { timeout: 3000 })).toBeInTheDocument()
  })
})
