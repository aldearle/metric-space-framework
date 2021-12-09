function h = plotHyperbolaLine(midFocalLength,halfDifference, left,right)
%ellipse is centred around the origin, e0 is major semi-axis
% which is therefore also the distance from the foci to the azimuth

a = halfDifference;
e = midFocalLength/halfDifference;
noOfPoints = 100;


theta = linspace(0, pi, noOfPoints);
xyCoords = ones(noOfPoints,2);

hyp = zeros(1,noOfPoints);
nom = a * (e^2 - 1);
lastPoint = zeros(1,2);
for i = 1:noOfPoints
    denom = 1 + e * cos(theta(i));
    %     if denom <= 0
    %         denom = lastDenom;
    %     end
    %     lastDenom = denom;
    
    
    r = nom/denom;
    theta(i);
    
    hyp(i) = r;
    
    xCoord = ( hyp(i) * cos(theta(i))-midFocalLength) ;
    yCoord = hyp(i) * sin(theta(i))
    
    if yCoord < 0
        yCoord = lastPoint(2);
        xCoord = lastPoint(1);
    end
    lastPoint(1) = xCoord;
    lastPoint(2) = yCoord;
    
    
    xyCoords(i,1) = xCoord ;
    xyCoords(i,2) = yCoord;
end

h = xyCoords;

% polar(theta,hyp);
% 
if( right )
    plot(xyCoords(:,1),xyCoords(:,2),'-','Color',[1,0,0]);
end
if( left)
    plot(-xyCoords(:,1),xyCoords(:,2),'-','Color',[1,0,0]);
end

end