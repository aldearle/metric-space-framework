dirname='/Volumes/Macintosh HD/Users/newrichard/Dropbox/sisapFourPointPaper/IS-supermetrics/experiments/MirFlickr/';
fname = 'nearDuplicates_MIRFlickr_hybrid_gmean_ReLu_fc6L2nomalized_EuclideanDist.txt';
fname2 = 'MIRFlickr_hybrid_gmean_ReLu_fc6L2nomalized_EuclideanDist.txt';

gist = readtable('/Volumes/Data/simplexes/output_temp/gist_jsd.csv')
t2 = readtable(strcat(dirname,fname2))

%%
h = figure;

hold on;
scatter(t2.actualDistance,t2.x500);
xy = linspace(0,0.7,1000);
plot(xy,xy);

title('GIST/JensenShannon 10 dimensional surrogate')
xlabel('original space distance')
ylabel('surrogate space distance')

hold off;