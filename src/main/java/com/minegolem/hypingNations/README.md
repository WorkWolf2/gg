# HypingNations

## Commands

### Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/hnations` | View information about your nation | `hypingnations.info` |
| `/hnations help` | Show all available commands | `hypingnations.help` |
| `/hnations create <name>` | Create a new nation from your city | `hypingnations.create` |
| `/hnations info` | View detailed information about your nation | `hypingnations.info` |
| `/hnations invitecity <city_name>` | Invite a city to join your nation | `hypingnations.invitecity` |
| `/hnations acceptcity` | Accept an invitation to join a nation | `hypingnations.acceptcity` |
| `/hnations leave` | Make your city leave its nation | `hypingnations.leave` |
| `/hnations tax` | View tax information for your nation | `hypingnations.tax` |

### Pact Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/hnations pact propose <nation> <days>` | Propose a diplomatic pact with another nation | `hypingnations.pact` |
| `/hnations pact accept <nation>` | Accept a pact proposal from another nation | `hypingnations.pact` |
| `/hnations pact deny <nation>` | Deny a pact proposal from another nation | `hypingnations.pact` |
| `/hnations pact break <nation>` | Break an active pact with another nation | `hypingnations.pact` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/hnations admin reload` | Reload the plugin configuration | `hypingnations.admin` |

## PlaceholderAPI Placeholders

**Identifier:** `hnations`

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%hnations_nation_name%` | The name of the player's nation | `Empire` |
| `%hnations_role%` | The player's role in their nation | `Chief`, `Mayor`, `Deputy Mayor`, `None` |
| `%hnations_tax_next_time%` | When the next tax collection will occur | `00:00 16/12/2025` |
| `%hnations_tax_amount%` | The amount of the next tax payment | `1500.00` |
| `%hnations_tax_chunks_count%` | Number of chunks being taxed | `10` |
| `%hnations_tax_unpaid_days%` | Number of consecutive days taxes haven't been paid | `2` |
| `%hnations_pacts_active_count%` | Number of active diplomatic pacts | `3` |
| `%hnations_range_effective_blocks%` | Effective recruitment range in blocks | `2000` |
| `%hnations_capital_name%` | Name of the nation's capital city | `MainCity` |
| `%hnations_member_cities_count%` | Number of cities in the nation | `5` |
| `%hnations_total_members_count%` | Total number of members across all cities | `47` |
| `%hnations_treasury%` | Amount in the nation's treasury | `50000.00` |
| `%hnations_chief_name%` | Name of the nation's chief | `PlayerName` |

## Command Aliases

- `/hnations` can also be used as:
    - `/hn`
    - `/nation`
    - `/nations`

## Notes

- All player commands default to `true` permission (available to all players)
- Admin commands require operator status by default
- Invitations to join nations expire after 30 minutes
- The capital city cannot leave a nation; the nation must be disbanded instead