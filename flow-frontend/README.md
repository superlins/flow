# Flow Frontend

Workflow management UI for the Flow platform.

## Features

- View all workflows with status indicators (DRAFT, ENABLED, DISABLED, ARCHIVED)
- Create new workflows
- Enable, disable, or archive existing workflows
- Execute enabled workflows with JSON input data
- View execution results including input/output and timing information

## Prerequisites

- Node.js 18+ and npm
- Flow backend API running on `http://localhost:8080` (or configured via `VITE_API_BASE_URL`)

## Getting Started

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Configuration

Create a `.env` file in the project root:

```
VITE_API_BASE_URL=http://localhost:8080
```

## Tech Stack

- **React 19** - UI library
- **TypeScript 5.9** - Type safety
- **Vite 7** - Build tool and dev server
- **TanStack Query** - Data fetching and caching
- **Axios** - HTTP client
- **Lucide React** - Icon library

## Project Structure

```
src/
├── api/           # API client functions
├── components/    # React components
├── types/         # TypeScript type definitions
├── App.tsx        # Main application component
└── main.tsx       # Application entry point
```

## API Integration

The frontend integrates with the Flow backend REST API:

- `GET /api/workflows` - List workflows
- `POST /api/workflows` - Create workflow
- `GET /api/workflows/{key}/{version}` - Get workflow details
- `POST /api/workflows/{key}/{version}/enable` - Enable workflow
- `POST /api/workflows/{key}/{version}/disable` - Disable workflow
- `POST /api/workflows/{key}/{version}/archive` - Archive workflow
- `POST /api/workflows/{key}/{version}/execute` - Execute workflow
- `GET /api/workflows/executions/{executionId}` - Get execution details
