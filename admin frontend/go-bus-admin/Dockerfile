# Build React application
FROM node:20-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps

# Install serve to host the build
RUN npm install -g serve

# Build arguments for environment variables
ARG REACT_APP_BASE_URL
ARG REACT_APP_WS_URL

# Copy source code
COPY . .

# Set environment variables for build and log them for debugging
ENV REACT_APP_BASE_URL=${REACT_APP_BASE_URL}
ENV REACT_APP_WS_URL=${REACT_APP_WS_URL}

# Debug: Print environment variables
RUN echo "Building with:" && \
    echo "REACT_APP_BASE_URL=${REACT_APP_BASE_URL}" && \
    echo "REACT_APP_WS_URL=${REACT_APP_WS_URL}"

# Build the application with environment variables
RUN npm run build

# Expose the port
EXPOSE 3000

# Serve the build folder - Railway sets PORT at runtime
CMD serve -s build -p ${PORT:-3000}
