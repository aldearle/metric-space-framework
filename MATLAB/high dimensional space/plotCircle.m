function h = plotCircle(centre,radius)
%ellipse is centred around the origin, e0 is major semi-axis
% which is therefore also the distance from the foci to the azimuth

noOfPoints = 50;
xOff = centre(1);
yOff = centre(2);

theta = linspace(0, pi/2, noOfPoints);

xyCoords = ones(noOfPoints,2);

for i = 1:noOfPoints
    
    xOffset = radius * sin(theta(i));
    yOffset = radius * cos(theta(i));
    
    xyCoords(i,1) = xOffset;
    xyCoords(i,2) = yOffset;
end

plot(xOff + xyCoords(:,1),yOff + xyCoords(:,2),'-','Color',[1,0,0]);
plot(xOff + xyCoords(:,1),yOff - xyCoords(:,2),'-','Color',[1,0,0]);
plot(xOff - xyCoords(:,1),yOff + xyCoords(:,2),'-','Color',[1,0,0]);
plot(xOff - xyCoords(:,1),yOff - xyCoords(:,2),'-','Color',[1,0,0]);


end