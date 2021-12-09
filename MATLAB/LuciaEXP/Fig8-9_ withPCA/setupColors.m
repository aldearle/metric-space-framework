

sisapNames = {'colors','nasa'};
seriesName = sisapNames(2);

dataSeriesName = strcat('SISAP {}',seriesName);
size = 500;
pivot1id = 1; % 3
pivot2id = 2; % 10

thresholds = zeros(1,3);
thresholds(1) = 0.052;
thresholds(2) = 0.083;
thresholds(3) = 0.131;

nasaThresholds = zeros(1,3);
nasaThresholds(1) = 0.12;
nasaThresholds(2) = 0.285;
nasaThresholds(3) = 0.53;

vecSize = 0;
dataVec = [];
if strcmp(seriesName,'colors')
    dataVec = colors;
    vecSize = 112;
    threshold = thresholds(1);
else
    dataVec = nasa;
    vecSize = 20;
    threshold = nasaThresholds(1);
end;
dataVec = dataVec(2:length(dataVec),:);

pivots = zeros(2,vecSize);
pivots(1,:) = dataVec(pivot1id,:);
pivots(2,:) = dataVec(pivot2id,:);
pDist = euc(pivots(1,:),pivots(2,:));


dists = zeros(size,2);
for i = 1 : size;
    dists(i,1) = euc(pivots(1,:),dataVec(i,:));
    dists(i,2) = euc(pivots(2,:),dataVec(i,:));
end
m = mean(dists(:,1));
s = std(dists(:,1));
idim = (2*m)/(s);
