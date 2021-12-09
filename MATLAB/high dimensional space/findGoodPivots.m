
%find the standard deviation and mean of the l/r splits for first 500 pairs
stdevs = zeros(500,4);

pivot1Index = 148;
thisPivot = dataVec(pivot1Index,:);
for i = 1 : 500
    pivot2 = dataVec(i,:);
    pivotDist = euc(thisPivot,pivot2);
    offsets = zeros(1,500);
    if pivotDist ~= 0
        for j = 1 : 500
            d1 = euc(pivot1,dataVec(j,:));
            d2 = euc(pivot2,dataVec(j,:));
            offsets(j) = (d1 * d1 - d2 * d2)/(2 * pivotDist);
        end
    end
    stdevs(i,1) = i;
    stdevs(i,2) = pivotDist;
    stdevs(i,3) = std(offsets(1,:));
    stdevs(i,4) = mean(offsets(1,:));
end