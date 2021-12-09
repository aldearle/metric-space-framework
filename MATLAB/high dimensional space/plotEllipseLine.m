function h = plotEllipseLine(e0,e1)
%ellipse is centred around the origin, e0 is major semi-axis
% which is therefore also the distance from the foci to the azimuth

theta = linspace(0,pi(),1000);
ell = zeros(1,1000);
for i = 1:1000
    e0sq = (e0 * sin(theta(i)))^2;
    e1sq = (e1 * cos(theta(i)))^2;
    ell(i) = (e0 * e1) / sqrt(e0sq + e1sq);
end

disp(size(ell));
disp(size(theta));
polar(theta,ell);

end