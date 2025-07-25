 Banda - P2P File Sharing Application

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/)
[![Next.js](https://img.shields.io/badge/Next.js-14-blue)](https://nextjs.org/)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-3-06B6D4)](https://tailwindcss.com/)

Banda is a peer-to-peer file sharing application that allows users to share files securely with others via unique invite codes.

## Features

- Simple, modern UI with drag-and-drop file upload
- Peer-to-peer file sharing with dynamic port assignment
- Automatic file type detection
- No user accounts or registration required
- Docker support for easy deployment

## Architecture Overview

Banda follows a client-server architecture with a unique P2P approach for actual file transfers:

![Architecture Diagram](https://mermaid.ink/img/pako:eNqNkl9rwjAUxb9KyNOGOuiLgvgwoWNuKOoc-yNkJG1uNZgmpWnZKH73JbXqmJt7SXLPOb-bm5s5iYTGJCHMclThsD66jbJmNdNGuaLeVbIsuZCiafswrueqsrCVkRDnN3CvK0HF8U5YqWaAoBuBs0djZAGzRsvXrjbgxbFx8VK8yZ3M-TUuuFPawRMynh2d48UJ_VhncJL7aKfKzBYyXGdQ0_ppq55_oGN_7JBL_ZaMnHZ0MIKr66vRsB8ZM63pyANSjHZKiiKC0KCEB3kvc0Ux7S9RMbxD6lO_hwwHtJ9eJD7jl5acHdO-9Gj2SfEp3YU8CKwslAjweOyPMPGQUQ9THlIqaK-PcXvnfwA-MKCD4RPNYRpQNqW9oTiK4FF76Qe0D6AUKO7QYx3Q3qd9Wtx5rs7s1Yok2O4lXpeWLKRrdRzH4KSfxM0GMCHy0JGETA3KgnLW5nlYm-w8PNc58aqwJBF0evj3TD_R5xd4JMQW?type=png)

### Backend Components

1. **BandaFileController**
   - Initializes the HTTP server and routes
   - Manages the upload directory and thread pool

2. **HTTP Handlers**
   - `FileUploadHandler`: Processes file uploads, generates ports
   - `FileDownloadHandler`: Handles download requests
   - `CORSHandler`: Manages cross-origin requests

3. **Core Services**
   - `Filesharer`: Manages file sharing via dynamic ports
   - `FileDownloadService`: Handles downloading files from ports

4. **Utilities**
   - `IntegratedFileParser`: Parses multipart form data efficiently
   - `FileExtensionHelper`: Detects file types using Apache Tika

### Frontend Components

1. **Pages**
   - page.tsx: Main application with upload/download tabs

2. **Components**
   - `FileUpload`: Drag-and-drop upload functionality
   - `FileDownload`: Interface for receiving files
   - `InviteCode`: Displays shareable invite codes

### Data Flow

![Data Flow Diagram](https://mermaid.ink/img/pako:eNqdlE9vozAUxL-K5VMrJQFCqkgVh02apsqfZrfZtj20Ujl4wRu8xkbYJmqrfPet3aRKmmTVHjBjef7Gzz4MJJKYJIKMJYomM7kMl-FNNeMVlTm8Z1qHD4WucNTQGl-9tdMJiSwTWskSx8HwRKq1RJMvhdGFHOZooxmOHcsPFN-zjK-jSs5HZKKEzFCE8_HwsNa5kMN4N9R1-oPoOI6G7RB8bkOi-CI1ZP-3EbEz_flzqAtz4HHcbs1M4HWPTDzikTueMMcKR_0Jc8ae50_8YBgGPrHboR-5c-L1VuTJEVNEBqx_Ao3lHLU4zkWBQ_YSXt9E19E0HN1Ex_hGZmgnSvDGzPWpey6iJ-QS7Z9drcAkVE_R9OLqa9F2hIy1XIVvgIaUmSwp1izS5MtcS6JfXGFGUim0qJrVhb-xiiqjlyRwxsOWihVmsqhYMhfsyY8js0zKNjyGKebJfDWyVtkP4_N5ZlzDkihVmGcso1mr_imJ5qGNZM2GbZayZBaDO5YsVKqUnO2XYLYLjdMhM2pHPjnOrNS35mvaHG0lpfshRhE32txHkwJs18Ud3MWnlvey3kC0coJ4HSWFTbAvtD_BvlDia78oWP3VBFd6-XY1OkpEunXT7cu6trdhIIeQULHONr8EqRor0lr-buI7QZLGSKJVmpS1puW9yOrszxVrWsoRhCR12MZJkh0iK3S23ovgW71_8ysO57VCJL-emrQkC4sYn57T-x9WFN0d?type=png)

1. **Upload Process:**
   - User uploads file in frontend
   - Backend receives file and assigns port
   - Backend starts dedicated server on that port
   - Frontend displays invite code (port number)

2. **Download Process:**
   - User enters invite code in frontend
   - Backend connects to the corresponding port
   - File is streamed through backend to frontend
   - Browser triggers file download

## Getting Started

### Prerequisites

- Java 21 or later
- Node.js 18 or later
- Docker and Docker Compose (optional)

### Running with Docker

The easiest way to run Banda is with Docker:

```bash
# Clone the repository
git clone https://github.com/yourusername/banda.git
cd banda

# Start the application
docker-compose up -d

# Access the application at http://localhost:3000
```

### Running Manually

#### Backend Setup

```bash
# Navigate to project directory
cd banda

# Build the project
mvn clean package

# Run the backend server
java -jar target/banda-1.0-SNAPSHOT.jar
```

#### Frontend Setup

```bash
# Navigate to UI directory
cd banda/ui

# Install dependencies
npm install

# Edit the Next.js configuration for local development
# Open next.config.js and change:
# - http://backend:8080 to http://localhost:8080

# Start the development server
npm run dev
```

> **Note**: When running manually, you need to modify the next.config.js file to point to `localhost:8080` instead of `backend:8080` for API calls.

## Usage

1. **Sharing a File:**
   - Go to the "Share a File" tab
   - Drag and drop a file or click to select one
   - Once uploaded, you'll receive an invite code (port number)
   - Share this invite code with anyone you want to share the file with

2. **Receiving a File:**
   - Go to the "Receive a File" tab
   - Enter the invite code you received
   - Click "Download File"
   - The file will be downloaded to your device

## Technical Details

### Backend (Java)

- Built with native Java HTTP Server (no Spring)
- Uses dynamic port assignment for P2P file transfer
- Efficient multipart file parsing with state machine approach
- Apache Tika for file type detection

### Frontend (Next.js)

- Built with Next.js 14 and React 18
- TailwindCSS for styling
- React Dropzone for drag-and-drop functionality
- Axios for API requests

## Project Structure

```
banda/
├── src/                     # Java backend code
│   ├── main/
│   │   ├── java/com/banda/  # Java source files
│   │   └── resources/       # Configuration files
│   └── test/                # Test files
├── ui/                      # Next.js frontend
│   ├── src/                 # Frontend source code
│   │   ├── app/             # Next.js app router
│   │   └── components/      # React components
│   └── public/              # Static assets
├── docker-compose.yml       # Docker Compose configuration
├── Dockerfile.be            # Backend Dockerfile
├── Dockerfile.fe            # Frontend Dockerfile
└── pom.xml                  # Maven configuration
```

## Building for Production

```bash
# Backend
mvn clean package

# Frontend
cd ui
npm run build
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Apache Tika for file type detection
- SLF4J and Logback for logging
- React Icons for UI icons
- TailwindCSS for styling
