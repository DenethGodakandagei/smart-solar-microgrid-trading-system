/**
 * Format energy capacity values for display.
 */

/**
 * Format kW/h value with appropriate unit.
 * @param {number} value - Energy value in kWh
 * @param {number} decimals - Decimal places (default: 2)
 * @returns {string}
 */
export function formatEnergy(value, decimals = 2) {
  if (value == null || isNaN(value)) return '—';

  if (value >= 1000) {
    return `${(value / 1000).toFixed(decimals)} MWh`;
  }

  return `${Number(value).toFixed(decimals)} kWh`;
}

/**
 * Format capacity value with kW unit.
 * @param {number} value - Capacity in kW
 * @returns {string}
 */
export function formatCapacity(value) {
  if (value == null || isNaN(value)) return '—';

  if (value >= 1000) {
    return `${(value / 1000).toFixed(1)} MW`;
  }

  return `${Number(value).toFixed(1)} kW`;
}
