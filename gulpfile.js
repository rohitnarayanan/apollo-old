'use strict';

var gulp = require('gulp'), autoprefixer = require('gulp-autoprefixer'), cssnano = require('gulp-cssnano'), jshint = require('gulp-jshint'), uglify = require('gulp-uglify'),
// imagemin = require('gulp-imagemin'),
rename = require('gulp-rename'), del = require('del');

gulp.task('styles', function() {
	return gulp.src('src/main/resources/static/css/**/*.css').pipe(
			autoprefixer('last 2 version')).pipe(gulp.dest('build/static/css'))
			.pipe(rename({
				suffix : '.min'
			})).pipe(cssnano()).pipe(gulp.dest('build/static/css'));
});

gulp.task('scripts', function() {
	return gulp.src('src/main/resources/static/js/**/*.js')
	// .pipe(jshint('.jshintrc'))
	// .pipe(jshint.reporter('default'))
	.pipe(gulp.dest('build/static/js')).pipe(rename({
		suffix : '.min'
	})).pipe(uglify()).pipe(gulp.dest('build/static/js'));
});

gulp.task('clean', function() {
	return del([ 'build/static' ]);
});

gulp.task('default', [ 'clean' ], function() {
	gulp.start('styles', 'scripts');
});