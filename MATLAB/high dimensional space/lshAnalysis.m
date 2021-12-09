% this started April 2016 to investigate the offset spread and think about
% LSH probabilities for different Hilbert spaces


possibleAnalyses = {'','horizontal','vertical','circle','farLeftDist'};
analysis = possibleAnalyses(3);

number = 500;

% first calcalate the actual XY coordinates in the plan
% these will always be plotted as absolutes
coords = zeros(2,number);
for i = 1 : number
    d1 = dists(i,1);
    d2 = dists(i,2);
    
    %a^2 = b^2 + c^2 - 2bc cosA
    % so.... cosA = (b^2 +cl c^2 - a^2)/2bc
    cos_piv_1 = (d1 * d1 + pDist * pDist - d2 * d2) / (2 * pDist);
    x_offset = cos_piv_1;
    y_offset = sqrt(d1 * d1 - x_offset * x_offset);
    
    coords(1,i) = x_offset - pDist/2;
    coords(2,i) = y_offset;
end

% calculate median x and y positions
medX = median(coords(1,:));
medY = median(coords(2,:));

% now calculate subsets for division of original space so that we can play
% with that later, not currently in any use
leftPartition = zeros(2,number);
rightPartition = zeros(2,number);
noOfLeftPoints = 0;
noOfRightPoints = 0;

for i = 1 : number
    leftCondition = false;
    if strcmp(analysis,'vertical')
        leftCondition = coords(1,i) < medX;
    elseif strcmp(analysis,'horizontal')
        leftCondition = coords(2,i) < medY;
    elseif strcmp(analysis,'circle')
       
    elseif strcmp(analysis,'farLeftDist')
       
    end;
    
    if leftCondition
        noOfLeftPoints = noOfLeftPoints + 1;
        leftPartition(1,noOfLeftPoints) = coords(1,i);
        leftPartition(2,noOfLeftPoints) = coords(2,i);
    else
        noOfRightPoints = noOfRightPoints + 1;
        rightPartition(1,noOfRightPoints) = coords(1,i);
        rightPartition(2,noOfRightPoints) = coords(2,i);
   end
end

% eventually we will also want cover radii for these partitions
leftPivot = [-pDist/2,0];
rightPivot = [pDist/2,0];

lCr = 0;
lMr = 10;
for i = 1 : noOfLeftPoints
    lCr = max(lCr,euc(leftPivot,leftPartition(:,i)'));
    lMr = min(lMr,euc(leftPivot,leftPartition(:,i)'));
end

rCr = 0;
rMr = 10;
for i = 1 : noOfRightPoints
    rCr = max(rCr,euc(rightPivot,rightPartition(:,i)'));
    rMr = min(rMr,euc(rightPivot,rightPartition(:,i)'));
end

%now calculate points with specific properties - keypoints are points that,
%were they to be queries, would not require to search the opposing subspace
keyPoints = zeros(2,number);
noOfKeyPoints = 0;

centre = [medX,medY];

%find median distance from centre
centreDists = zeros(1,number);
for i = 1 : number 
    centreDists(i) = euc(coords(:,i)',[medX,medY]);
end
medianDistFromCentre = median(centreDists);


% let's find distances from the bottom-left corner of the diagram
minX = min(coords(1,:));
minY = min(coords(2,:));
maxX = max(coords(1,:));
maxY = max(coords(2,:));
botLeft = [minX,maxY];
botLeftDists = zeros(1,number);
for i = 1 : number 
    botLeftDists(i) = euc(coords(:,i)',botLeft);
end
medianBotLeftDist = median(botLeftDists);

for i = 1 : number
    condition = false;
    
    if strcmp(analysis,'vertical')
        condition = abs(coords(1,i) - medX) > threshold;
    elseif strcmp(analysis,'horizontal')
        condition = abs(coords(2,i) - medY) > threshold;
    elseif strcmp(analysis,'circle')
        
        condition = abs(euc(coords(:,i)',centre) - medianDistFromCentre) > threshold;
    elseif strcmp(analysis,'farLeftDist')
        %         take an arbitrary point at (minX, minY) and partition by
        %         distance from that; assume
        
        
        condition = abs(euc(coords(:,i)',botLeft) - medianBotLeftDist) > threshold;
    end;
    
    if condition
        noOfKeyPoints = noOfKeyPoints + 1;
        keyPoints(1,noOfKeyPoints) = coords(1,i);
        keyPoints(2,noOfKeyPoints) = coords(2,i);
   end
end


h = figure;
title_text = strcat(analysis, '');

title(strcat(title_text, dataSeriesName));
xlabel('X');
ylabel('altitude from line (p_1,p_2)')
whitespace = 1.1;
xSpread = max(coords(1,:))-min(coords(1,:));
ySpread = max(coords(2,:));
bigger  = max(xSpread,ySpread) * whitespace;
% axis([-bigger/2,bigger/2,0,bigger]);
yAxisLimit = max(coords(2,:))*whitespace;
xAxisMin = min(coords(1,:))*whitespace;
xAxisMax =  max(coords(1,:))*whitespace; 
axis([min(coords(1,:))*whitespace,max(coords(1,:))*whitespace,0,max(coords(2,:))*whitespace]);
hold on;

% plot the points
% this line is just to test and won't usually be uncommented
 p1 = scatter(coords(1,:),coords(2,:),50,[0,0,0]);

keys = scatter(keyPoints(1,1:noOfKeyPoints),keyPoints(2,1:noOfKeyPoints),50,[0,0,0],'filled');

pivots = scatter([-pDist/2,pDist/2],[0,0],50,[0,0,0]);

p2_label = strcat('exclusive queries, n = {}',num2str(noOfKeyPoints));
legend([p1,keys],'non-exclusive queries',p2_label)


if strcmp(analysis,'vertical')
    plot(linspace(medX-threshold,medX-threshold,100),linspace(0,yAxisLimit,100),'-','Color',[1,0,0]);
    plot(linspace(medX+threshold,medX+threshold,100),linspace(0,yAxisLimit,100),'-','Color',[1,0,0]);
elseif strcmp(analysis,'horizontal');
    
    plot(linspace(xAxisMin,xAxisMax,100),linspace(medY-threshold,medY-threshold,100),'-','Color',[1,0,0]);
    plot(linspace(xAxisMin,xAxisMax,100),linspace(medY+threshold,medY+threshold,100),'-','Color',[1,0,0]);
elseif strcmp(analysis,'circle')
    plotCircle(centre, medianDistFromCentre - threshold);
    plotCircle(centre, medianDistFromCentre + threshold);
elseif strcmp(analysis,'farLeftDist')
    plotCircle(botLeft, medianBotLeftDist - threshold);
    plotCircle(botLeft, medianBotLeftDist + threshold);
end;

% plot(linspace(threshold,threshold,10),linspace(0,2,10),'-','Color',[1,0,0]);
% plot(linspace(-threshold,-threshold,10),linspace(0,2,10),'-','Color',[1,0,0]);


hold off;

% p2_label = strcat('exclusive queries, n = {}',num2str(no_of_discards));
% legend([p1,p2],'non-exclusive queries',p2_label)
% pic_filename = 'partition_plot2_';

% pic_filename = strcat(pic_filename,'hilbert_lsh_test');

% saveas(h,'lastPlot.png');
% 
% h2 = figure;
% title_text = 'Four point X-offsets, {}';
% title(strcat(title_text, num2str(pDist), ' pivot separation'));
% hold on;
% hist = histogram(coords(1,:),'Normalization','cdf');
% hold off;
