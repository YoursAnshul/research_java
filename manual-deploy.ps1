param(
	[string]$env,
	[string]$branch
)
 
# Configuration
$imageName = "pro-app"
$acRegistry = "acrproprod"
 
# Image path
$imagePath = $acRegistry + ".azurecr.io/" + $env + "/pro-app"
 
# Switch to the specified branch
git switch $branch
 
# Pull the latest changes
git pull
 
# Azure CLI and ACR login
az login
az acr login --name $acRegistry
 
# Build
docker build -t $imageName .
 
# Tag and push
docker tag $imageName $imagePath
docker push $imagePath