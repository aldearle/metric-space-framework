p1 = rand(1,14);
p2 = rand(1,14);
p3 = rand(1,14);

fLen = euc(p1,p2);
a = euc(p1,p3);
b = euc(p2,p3);
e0 = (euc(p1,p3) + euc(p2,p3)) / 2;
cosTheta = ((a^2 + fLen^2) - b^2) / (2 * a * fLen);
newX = a * cosTheta;
newY = sqrt(a^2 - newX^2);

newXs = [0-fLen/2,fLen- fLen/2,newX-fLen/2];
newYs = [0,0,newY];

fLenDash = euc([newXs(1),newYs(1)],[newXs(2),newYs(2)]);
aDash = euc([newXs(1),newYs(1)],[newXs(3),newYs(3)]);
bDash = euc([newXs(2),newYs(2)],[newXs(3),newYs(3)]);

e0 = (a + b) / 2;
e1 = sqrt((e0)^2 - (fLen/2)^2);


figure
hold on;

plotEllipseLine(e0,e1);

scatter([p1(1),p2(1),p3(1)],[p1(2),p2(2),p3(2)]);
scatter(newXs,newYs);


hold off;

