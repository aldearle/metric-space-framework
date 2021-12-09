
closePivots = false;
farPivots = false;

currentDim = 6;
thresholds = zeros(1,14);
thresholds(6) = 0.071574561;
thresholds(8) = 0.144614658;
thresholds(10) = 0.235107454;
thresholds(12) = 0.325980644;
thresholds(14) = 0.420376768;


dataSeriesName = strcat(num2str(currentDim), ' dimensional generated')
threshold = thresholds(currentDim);
size = 500;
data = rand(size,currentDim);

pivots = rand(2,currentDim);
pDist = euc(pivots(1,:),pivots(2,:));

if closePivots
    for p = 1 : 1000
        newPivs = rand(2,currentDim);
        newPDist = euc(newPivs(1,:),newPivs(2,:));
        if newPDist < pDist
            pivots = newPivs;
            pDist = newPDist;
        end
    end
end

if farPivots
    for p = 1 : 1000
        newPivs = rand(2,currentDim);
        newPDist = euc(newPivs(1,:),newPivs(2,:));
        if newPDist > pDist
            pivots = newPivs;
            pDist = newPDist;
        end
    end
end

dists = zeros(size,2);
for i = 1 : size;
    dists(i,1) = euc(pivots(1,:),data(i,:));
    dists(i,2) = euc(pivots(2,:),data(i,:));
end
m = mean(dists(:,1));
s = std(dists(:,1));
idim = (2*m)/(s);
