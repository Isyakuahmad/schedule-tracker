# Use ultra-lightweight Nginx alpine image (~15MB)
FROM nginx:alpine

# Copy web app files to Nginx public directory
COPY web_app/ /usr/share/nginx/html/

# Expose standard web port
EXPOSE 80

# Start Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
